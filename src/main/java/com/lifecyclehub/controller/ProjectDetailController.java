package com.lifecyclehub.controller;

import com.lifecyclehub.entity.Project;
import com.lifecyclehub.entity.Stage;
import com.lifecyclehub.entity.Task;
import com.lifecyclehub.repository.StageRepository;
import com.lifecyclehub.repository.TaskRepository;
import com.lifecyclehub.service.PdfExportService;
import com.lifecyclehub.service.ProgressService;
import com.lifecyclehub.service.ProjectService;
import com.lifecyclehub.service.TaskService;
import com.lifecyclehub.ui.WindowManager;
import com.lifecyclehub.util.ThemeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ProjectDetailController {

    // ── FXML Bindings ─────────────────────────────────────────
    @FXML private Label       projectNameLabel;
    @FXML private Label       stageLabel;
    @FXML private Label       progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private HBox        stageStepperBox;
    @FXML private VBox        stagesContainer;
    @FXML private Button      archiveBtn;
    @FXML private Button      editProjectBtn;
    @FXML private Button      exportPdfBtn;
    @FXML private Button      themeToggleBtn;
    @FXML private Button      sidebarIdea;
    @FXML private Button      sidebarPlanning;
    @FXML private Button      sidebarBuilding;
    @FXML private Button      sidebarTesting;
    @FXML private Button      sidebarLaunch;
    @FXML private Button      sidebarMaintenance;

    // ── Services ──────────────────────────────────────────────
    private final ProjectService   projectService   = new ProjectService();
    private final TaskService      taskService      = new TaskService();
    private final ProgressService  progressService  = new ProgressService();
    private final PdfExportService pdfExportService = new PdfExportService();
    private final StageRepository  stageRepository  = new StageRepository();
    private final TaskRepository   taskRepository   = new TaskRepository();

    // ── State ─────────────────────────────────────────────────
    private int     projectId;
    private Project project;
    private String  activeStageTab = null;

    // ─────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        System.out.println("[ProjectDetailController] Initialized.");
        themeToggleBtn.setText(ThemeManager.getToggleLabel());
    }

    public void initData(int projectId) {
        this.projectId = projectId;
        this.project   = projectService.getProjectById(projectId);
        if (project == null) { projectNameLabel.setText("Project not found"); return; }
        activeStageTab = project.getCurrentStage();
        loadHeader();
        loadStages();
    }

    // ── Header ────────────────────────────────────────────────

    private void loadHeader() {
        projectNameLabel.setText(project.getName());
        stageLabel.setText("Stage: " + project.getCurrentStage());
        double fraction = progressService.calculateProgressFraction(projectId);
        progressBar.setProgress(fraction);
        progressLabel.setText((int)(fraction * 100) + "%");
        archiveBtn.setText(project.isArchived() ? "📂 Unarchive" : "🗄 Archive");
    }

    // ── Stage Stepper ─────────────────────────────────────────

    private void loadStages() {
        List<Stage> stages = stageRepository.findByProjectId(projectId);
        buildStageStepper(stages);
        wireSidebarButtons(stages);
        Stage active = stages.stream()
            .filter(s -> s.getName().equals(activeStageTab))
            .findFirst().orElse(stages.isEmpty() ? null : stages.get(0));
        if (active != null) { activeStageTab = active.getName(); showStageContent(active); }
        updateProgress();
    }

    private void buildStageStepper(List<Stage> stages) {
        stageStepperBox.getChildren().clear();
        for (Stage stage : stages) {
            Button tab = new Button(stage.getName());
            tab.getStyleClass().clear();
            tab.getStyleClass().add("stage-tab");
            if (stage.getName().equals(activeStageTab))
                tab.getStyleClass().add("stage-tab-active");
            Stage fs = stage;
            tab.setOnAction(e -> navigateToStage(fs, stages));
            stageStepperBox.getChildren().add(tab);
        }
    }

    private void wireSidebarButtons(List<Stage> stages) {
        Button[] btns  = {sidebarIdea,sidebarPlanning,sidebarBuilding,
                          sidebarTesting,sidebarLaunch,sidebarMaintenance};
        String[] names = {"IDEA","PLANNING","BUILDING","TESTING","LAUNCH","MAINTENANCE"};
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] == null) continue;
            btns[i].getStyleClass().remove("sidebar-stage-btn-active");
            if (names[i].equals(activeStageTab))
                btns[i].getStyleClass().add("sidebar-stage-btn-active");
            String n = names[i];
            Stage matched = stages.stream()
                .filter(s -> s.getName().equals(n)).findFirst().orElse(null);
            if (matched != null) {
                Stage fs = matched;
                btns[i].setOnAction(e -> navigateToStage(fs, stages));
            }
        }
    }

    private void navigateToStage(Stage stage, List<Stage> all) {
        activeStageTab = stage.getName();
        buildStageStepper(all);
        wireSidebarButtons(all);
        showStageContent(stage);
    }

    private void showStageContent(Stage stage) {
        stagesContainer.getChildren().clear();
        stagesContainer.getChildren().add(buildStagePanel(stage));
    }

    // ── Stage Panel ───────────────────────────────────────────

    private VBox buildStagePanel(Stage stage) {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("stage-panel");

        // ── Stage header row ──────────────────────────────────
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label stageNameLbl = new Label(stage.getName());
        stageNameLbl.getStyleClass().add("stage-header-label");

        List<Task> tasks = taskRepository.findByStageId(stage.getId());
        long done = tasks.stream().filter(Task::isCompleted).count();
        Label countLbl = new Label("  " + done + " / " + tasks.size() + " tasks");
        countLbl.getStyleClass().add("stage-task-count");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // "Phase Overview" toggle — renamed from generic "Notes"
        boolean hasOverview = stage.getNotes() != null && !stage.getNotes().isBlank();
        Button overviewToggle = new Button(hasOverview ? "Phase Overview ●" : "Phase Overview");
        overviewToggle.getStyleClass().add("notes-toggle-btn");

        header.getChildren().addAll(stageNameLbl, countLbl, spacer, overviewToggle);
        panel.getChildren().add(header);

        // ── Phase Overview section (stage-level notes) ────────
        VBox overviewSection = buildPhaseOverviewSection(stage, overviewToggle);
        overviewSection.setVisible(false);
        overviewSection.setManaged(false);
        overviewToggle.setOnAction(e -> {
            boolean showing = overviewSection.isVisible();
            overviewSection.setVisible(!showing);
            overviewSection.setManaged(!showing);
        });
        panel.getChildren().add(overviewSection);

        // ── Add task bar ──────────────────────────────────────
        panel.getChildren().add(buildAddTaskBar(stage));

        // ── Task list ─────────────────────────────────────────
        if (tasks.isEmpty()) {
            Label el = new Label("No tasks yet — draft one above.");
            el.setStyle("-fx-font-size:12px; -fx-text-fill:#2D2D2D; -fx-padding:6 0 6 0;");
            panel.getChildren().add(el);
        } else {
            VBox taskList = new VBox(4);
            for (Task task : tasks) {
                taskList.getChildren().add(buildCollapsibleTaskRow(task, stage));
            }
            panel.getChildren().add(taskList);
        }

        Region divider = new Region();
        divider.getStyleClass().add("divider-line");
        divider.setMaxWidth(Double.MAX_VALUE);
        panel.getChildren().add(divider);
        return panel;
    }

    // ── Phase Overview Section (stage-level notes, renamed) ───

    private VBox buildPhaseOverviewSection(Stage stage, Button toggleBtn) {
        VBox section = new VBox(8);
        section.getStyleClass().add("notes-section");

        // Label it clearly as "Phase Overview"
        Label headerLbl = new Label("PHASE OVERVIEW");
        headerLbl.setStyle(
            "-fx-font-size:9px; -fx-font-weight:bold; " +
            "-fx-text-fill:#555555; -fx-letter-spacing:0.12em;");

        TextArea notesArea = new TextArea(stage.getNotes() != null ? stage.getNotes() : "");
        notesArea.setPromptText(
            "Describe the goals, decisions, and context for this stage...");
        notesArea.getStyleClass().add("notes-textarea");
        notesArea.setPrefRowCount(4);
        notesArea.setWrapText(true);

        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button saveBtn = new Button("Save Overview");
        saveBtn.getStyleClass().add("btn-save-notes");
        saveBtn.setOnAction(e -> {
            stageRepository.updateNotes(stage.getId(), notesArea.getText());
            stage.setNotes(notesArea.getText());
            toggleBtn.setText(!notesArea.getText().isBlank()
                ? "Phase Overview ●" : "Phase Overview");
            saveBtn.setText("✓ Saved");
            new Thread(() -> {
                try { Thread.sleep(1800); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> saveBtn.setText("Save Overview"));
            }).start();
        });
        btnRow.getChildren().add(saveBtn);
        section.getChildren().addAll(headerLbl, notesArea, btnRow);
        return section;
    }

    // ── Collapsible Task Row (with task notes) ────────────────

    private VBox buildCollapsibleTaskRow(Task task, Stage stage) {
        VBox container = new VBox(0);
        container.getStyleClass().add("task-container");

        // ── Main task header row ──
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("task-row");
        if (!task.isCompleted() && task.getDueDate() != null && task.isOverdue())
            row.getStyleClass().add("task-row-overdue");
        if (task.isCompleted())
            row.getStyleClass().add("task-row-completed");

        // Radio complete button
        Button radio = new Button(task.isCompleted() ? "✓" : "");
        radio.getStyleClass().clear();
        radio.getStyleClass().add(task.isCompleted() ? "task-radio-done" : "task-radio");
        if (task.isCompleted())
            radio.setStyle("-fx-text-fill:rgba(255,255,255,0.45); -fx-font-size:10px;");
        radio.setOnAction(e -> {
            taskService.toggleTask(task);
            refreshStage(stage);
            updateProgress();
        });

        // Priority badge
        Label priorityBadge = buildPriorityBadge(task.getPriority());

        // Task title
        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add(task.isCompleted() ? "task-title-done" : "task-title");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Due date badge
        Label dueBadge = buildDueDateBadge(task);

        // Notes expand button — shows dot if task has notes
        Button notesBtn = new Button(task.hasNotes() ? "· · ·" : "···");
        notesBtn.getStyleClass().add("btn-task-notes-toggle");
        notesBtn.setStyle(task.hasNotes()
            ? "-fx-text-fill:#89B4FA; -fx-font-size:11px; -fx-padding:3 7 3 7;"
            : "-fx-text-fill:#444; -fx-font-size:11px; -fx-padding:3 7 3 7;");

        // Priority button
        Button priorityBtn = new Button("⚑");
        priorityBtn.getStyleClass().add("btn-date-inline");
        priorityBtn.setStyle("-fx-font-size:11px; -fx-padding:3 7 3 7;");
        priorityBtn.setOnAction(e -> handleSetPriority(task, stage));

        // Date button
        Button dateBtn = new Button("📅");
        dateBtn.getStyleClass().add("btn-date-inline");
        dateBtn.setStyle("-fx-font-size:11px; -fx-padding:3 7 3 7;");
        dateBtn.setOnAction(e -> handleSetDueDate(task, stage));

        // Delete button
        Button delBtn = new Button("✕");
        delBtn.getStyleClass().add("btn-icon");
        delBtn.setStyle("-fx-font-size:10px; -fx-text-fill:#2D2D2D; -fx-padding:3 6 3 6;");
        delBtn.setOnAction(e -> {
            taskService.deleteTask(task.getId());
            refreshStage(stage);
            updateProgress();
        });

        row.getChildren().addAll(radio, priorityBadge, titleLabel,
            dueBadge, notesBtn, priorityBtn, dateBtn, delBtn);

        // ── Task notes panel (collapsed by default) ───────────
        VBox taskNotesPanel = buildTaskNotesPanel(task, notesBtn);
        taskNotesPanel.setVisible(false);
        taskNotesPanel.setManaged(false);

        notesBtn.setOnAction(e -> {
            boolean showing = taskNotesPanel.isVisible();
            taskNotesPanel.setVisible(!showing);
            taskNotesPanel.setManaged(!showing);
        });

        container.getChildren().addAll(row, taskNotesPanel);
        return container;
    }

    // ── Task Notes Panel ──────────────────────────────────────

    private VBox buildTaskNotesPanel(Task task, Button toggleBtn) {
        VBox panel = new VBox(6);
        panel.getStyleClass().add("task-notes-panel");
        panel.setPadding(new Insets(8, 12, 10, 38)); // indented to align with task title

        Label headerLbl = new Label("TASK NOTES");
        headerLbl.setStyle(
            "-fx-font-size:9px; -fx-font-weight:bold; " +
            "-fx-text-fill:#444444; -fx-letter-spacing:0.12em;");

        TextArea notesArea = new TextArea(task.getNotes());
        notesArea.setPromptText("Add implementation details, blockers, or references...");
        notesArea.getStyleClass().add("task-notes-textarea");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        HBox btnRow = new HBox(8);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("btn-save-notes");
        saveBtn.setOnAction(e -> {
            String text = notesArea.getText();
            taskService.setTaskNotes(task.getId(), text);
            task.setNotes(text);

            // Update the expand button style to indicate notes exist
            boolean hasNotes = !text.isBlank();
            toggleBtn.setText(hasNotes ? "· · ·" : "···");
            toggleBtn.setStyle(hasNotes
                ? "-fx-text-fill:#89B4FA; -fx-font-size:11px; -fx-padding:3 7 3 7;"
                : "-fx-text-fill:#444; -fx-font-size:11px; -fx-padding:3 7 3 7;");

            saveBtn.setText("✓ Saved");
            new Thread(() -> {
                try { Thread.sleep(1800); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> saveBtn.setText("Save"));
            }).start();
        });

        btnRow.getChildren().add(saveBtn);
        panel.getChildren().addAll(headerLbl, notesArea, btnRow);
        return panel;
    }

    // ── Priority Badge ────────────────────────────────────────

    private Label buildPriorityBadge(Task.Priority priority) {
        Label badge = new Label();
        if (priority == null || priority == Task.Priority.NONE) {
            badge.setVisible(false); badge.setManaged(false); return badge;
        }
        badge.setText(priority.label());
        badge.getStyleClass().add("badge-priority-" + priority.name().toLowerCase());
        return badge;
    }

    // ── Priority Dialog ───────────────────────────────────────

    private void handleSetPriority(Task task, Stage stage) {
        Dialog<Task.Priority> dialog = new Dialog<>();
        dialog.setTitle("Set Priority");
        dialog.setHeaderText("Priority for: " + task.getTitle());
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/obsidian.css").toExternalForm());
        dialog.initOwner(WindowManager.getPrimaryStage());

        ButtonType setBtn   = new ButtonType("Set",   ButtonBar.ButtonData.OK_DONE);
        ButtonType clearBtn = new ButtonType("Clear", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(setBtn, clearBtn, ButtonType.CANCEL);

        ToggleGroup group = new ToggleGroup();
        RadioButton rbHigh   = new RadioButton("! HIGH   — urgent, must be done immediately");
        RadioButton rbMedium = new RadioButton("~ MED    — important but not blocking");
        RadioButton rbLow    = new RadioButton("▽ LOW    — nice to have, can wait");
        rbHigh.setToggleGroup(group);
        rbMedium.setToggleGroup(group);
        rbLow.setToggleGroup(group);
        rbHigh.setStyle("-fx-text-fill:#CCC; -fx-font-size:12px;");
        rbMedium.setStyle("-fx-text-fill:#CCC; -fx-font-size:12px;");
        rbLow.setStyle("-fx-text-fill:#CCC; -fx-font-size:12px;");

        switch (task.getPriority()) {
            case HIGH   -> rbHigh.setSelected(true);
            case MEDIUM -> rbMedium.setSelected(true);
            case LOW    -> rbLow.setSelected(true);
            default     -> rbMedium.setSelected(true);
        }

        VBox content = new VBox(10);
        content.setPadding(new Insets(16, 4, 4, 4));
        content.getChildren().addAll(rbHigh, rbMedium, rbLow);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == clearBtn) return Task.Priority.NONE;
            if (btn == setBtn) {
                if (rbHigh.isSelected())   return Task.Priority.HIGH;
                if (rbMedium.isSelected()) return Task.Priority.MEDIUM;
                if (rbLow.isSelected())    return Task.Priority.LOW;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            taskService.setPriority(task.getId(), p);
            task.setPriority(p);
            refreshStage(stage);
        });
    }

    // ── Due Date Badge ────────────────────────────────────────

    private Label buildDueDateBadge(Task task) {
        Label badge = new Label();
        if (task.isCompleted()) {
            badge.setText("DONE");
            badge.getStyleClass().add("badge-completed");
            return badge;
        }
        if (task.getDueDate() == null) {
            badge.setVisible(false); badge.setManaged(false); return badge;
        }
        if (task.isOverdue()) {
            badge.setText("OVERDUE");
            badge.getStyleClass().add("badge-overdue");
        } else if (task.isDueSoon()) {
            badge.setText("DUE SOON");
            badge.getStyleClass().add("badge-due-soon");
        } else {
            long days = java.time.temporal.ChronoUnit.DAYS
                .between(LocalDate.now(), task.getDueDate());
            badge.setText(days <= 7
                ? days + " DAYS LEFT"
                : "Due " + task.getDueDate().format(DateTimeFormatter.ofPattern("MMM d")));
            badge.getStyleClass().add("badge-due-normal");
        }
        return badge;
    }

    // ── Due Date Dialog ───────────────────────────────────────

    private void handleSetDueDate(Task task, Stage stage) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("Set Due Date");
        dialog.setHeaderText("Due date for: " + task.getTitle());
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/obsidian.css").toExternalForm());
        dialog.initOwner(WindowManager.getPrimaryStage());

        ButtonType setBtn   = new ButtonType("Set Date",   ButtonBar.ButtonData.OK_DONE);
        ButtonType clearBtn = new ButtonType("Clear Date", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(setBtn, clearBtn, ButtonType.CANCEL);

        DatePicker picker = new DatePicker(task.getDueDate());
        dialog.getDialogPane().setContent(picker);

        dialog.setResultConverter(btn -> {
            if (btn == setBtn)   return picker.getValue();
            if (btn == clearBtn) return LocalDate.MIN;
            return null;
        });

        dialog.showAndWait().ifPresent(date -> {
            taskService.setDueDate(task.getId(), date.equals(LocalDate.MIN) ? null : date);
            refreshStage(stage);
        });
    }

    // ── Add Task Bar ──────────────────────────────────────────

    private HBox buildAddTaskBar(Stage stage) {
        HBox bar = new HBox(8);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("add-task-bar");

        Label plus = new Label("+");
        plus.setStyle("-fx-text-fill:#444; -fx-font-size:14px; -fx-padding:0 4 0 0;");

        TextField taskField = new TextField();
        taskField.setPromptText("Draft a new task...");
        taskField.getStyleClass().add("add-task-field");
        HBox.setHgrow(taskField, Priority.ALWAYS);

        ComboBox<String> priorityPicker = new ComboBox<>();
        priorityPicker.getItems().addAll("No Priority", "! High", "~ Medium", "▽ Low");
        priorityPicker.setValue("No Priority");
        priorityPicker.setPrefWidth(105);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date");
        datePicker.setPrefWidth(100);

        Button addBtn = new Button("Add");
        addBtn.getStyleClass().add("btn-add-task");

        Runnable addAction = () -> {
            String title = taskField.getText().trim();
            if (!title.isEmpty()) {
                Task.Priority priority = switch (priorityPicker.getValue()) {
                    case "! High"   -> Task.Priority.HIGH;
                    case "~ Medium" -> Task.Priority.MEDIUM;
                    case "▽ Low"    -> Task.Priority.LOW;
                    default         -> Task.Priority.NONE;
                };
                taskService.addTask(title, stage.getName(), projectId,
                    datePicker.getValue(), priority);
                taskField.clear();
                datePicker.setValue(null);
                priorityPicker.setValue("No Priority");
                refreshStage(stage);
                updateProgress();
            }
        };

        taskField.setOnAction(e -> addAction.run());
        addBtn.setOnAction(e -> addAction.run());
        bar.getChildren().addAll(plus, taskField, priorityPicker, datePicker, addBtn);
        return bar;
    }

    // ── Edit Project Dialog ───────────────────────────────────

    @FXML
    public void handleEditProject() {
        if (project == null) return;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Project");
        dialog.setHeaderText("Edit \"" + project.getName() + "\"");
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/obsidian.css").toExternalForm());
        dialog.initOwner(WindowManager.getPrimaryStage());

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        VBox content = new VBox(8);
        content.setPadding(new Insets(16, 4, 4, 4));

        Label lName = new Label("Project Name");
        lName.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");
        TextField nameField = new TextField(project.getName());
        nameField.getStyleClass().add("text-field");

        Label lDesc = new Label("Description");
        lDesc.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");
        TextField descField = new TextField(
            project.getDescription() != null ? project.getDescription() : "");
        descField.getStyleClass().add("text-field");

        content.getChildren().addAll(lName, nameField, lDesc, descField);
        dialog.getDialogPane().setContent(content);
        Platform.runLater(nameField::requestFocus);

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                String newName = nameField.getText().trim();
                if (!newName.isEmpty()) {
                    projectService.editProject(project.getId(), newName,
                        descField.getText().trim());
                    project.setName(newName);
                    project.setDescription(descField.getText().trim());
                    loadHeader();
                }
            }
        });
    }

    // ── Progress + Refresh ────────────────────────────────────

    private void updateProgress() {
        double fraction = progressService.calculateProgressFraction(projectId);
        progressBar.setProgress(fraction);
        progressLabel.setText((int)(fraction * 100) + "%");
    }

    private void refreshStage(Stage stage) {
        List<Stage> stages = stageRepository.findByProjectId(projectId);
        Stage fresh = stages.stream()
            .filter(s -> s.getId() == stage.getId())
            .findFirst().orElse(stage);
        buildStageStepper(stages);
        wireSidebarButtons(stages);
        showStageContent(fresh);
    }

    // ── Action Handlers ───────────────────────────────────────

    @FXML public void handleBack() { WindowManager.showDashboard(); }

    @FXML public void handleThemeToggle() {
        ThemeManager.toggle();
        ThemeManager.applyCurrentTheme();
        themeToggleBtn.setText(ThemeManager.getToggleLabel());
    }

    @FXML public void handleArchive() {
        if (project == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText((project.isArchived() ? "Unarchive" : "Archive")
            + " \"" + project.getName() + "\"?");
        confirm.setContentText(project.isArchived()
            ? "Move back to active projects."
            : "Hide from dashboard. Restore anytime.");
        confirm.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/obsidian.css").toExternalForm());
        confirm.initOwner(WindowManager.getPrimaryStage());
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (project.isArchived()) projectService.unarchiveProject(project.getId());
                else                      projectService.archiveProject(project.getId());
                WindowManager.showDashboard();
            }
        });
    }

    @FXML public void handleExportPdf() {
        if (project == null) return;
        try {
            File pdfFile = pdfExportService.exportProject(project);
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Export Successful");
            info.setHeaderText("PDF Report Generated");
            info.setContentText("Saved to:\n" + pdfFile.getAbsolutePath());
            info.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/obsidian.css").toExternalForm());
            info.initOwner(WindowManager.getPrimaryStage());
            info.showAndWait();
        } catch (Exception e) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Export Failed");
            err.setHeaderText("Could not generate PDF");
            err.setContentText(e.getMessage());
            err.initOwner(WindowManager.getPrimaryStage());
            err.showAndWait();
        }
    }
}
