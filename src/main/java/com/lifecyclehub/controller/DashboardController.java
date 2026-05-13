package com.lifecyclehub.controller;

import com.lifecyclehub.entity.Project;
import com.lifecyclehub.service.ProjectService;
import com.lifecyclehub.service.ProgressService;
import com.lifecyclehub.ui.WindowManager;
import com.lifecyclehub.util.ThemeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private FlowPane  projectsGrid;
    @FXML private Label     statusLabel;
    @FXML private Label     dashboardTitleLabel;
    @FXML private Label     statActiveProjects;
    @FXML private Label     statTotalCompletion;
    @FXML private Label     statUpcomingDeadlines;
    @FXML private Button    themeToggleBtn;
    @FXML private Button    showArchivedBtn;
    @FXML private TextField searchField;
    @FXML private VBox      emptyState;

    @FXML private Button sidebarIdea;
    @FXML private Button sidebarPlanning;
    @FXML private Button sidebarBuilding;
    @FXML private Button sidebarTesting;
    @FXML private Button sidebarLaunch;
    @FXML private Button sidebarMaintenance;

    private final ProjectService  projectService  = new ProjectService();
    private final ProgressService progressService = new ProgressService();

    private boolean showingArchived = false;
    private String  searchQuery     = "";

    @FXML
    public void initialize() {
        System.out.println("[DashboardController] Initializing...");
        themeToggleBtn.setText(ThemeManager.getToggleLabel());

        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, nw) -> {
                searchQuery = nw == null ? "" : nw.trim().toLowerCase();
                renderProjects(getVisibleProjects());
            });
        }

        ThemeManager.applyCurrentTheme();
        loadProjects();
    }

    private List<Project> getVisibleProjects() {
        List<Project> base = showingArchived
                ? projectService.getArchivedProjects()
                : projectService.getActiveProjects();

        if (searchQuery.isEmpty()) return base;
        return base.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchQuery)
                        || (p.getDescription() != null
                        && p.getDescription().toLowerCase().contains(searchQuery)))
                .collect(Collectors.toList());
    }

    private void loadProjects() {
        renderProjects(getVisibleProjects());
        updateStatCards();
        setStatus(projectService.getActiveProjects().size() + " active projects");
    }

    private void renderProjects(List<Project> projects) {
        projectsGrid.getChildren().clear();

        boolean isEmpty = projects.isEmpty() && searchQuery.isEmpty();
        if (emptyState != null) {
            emptyState.setVisible(isEmpty);
            emptyState.setManaged(isEmpty);
        }

        for (Project p : projects) {
            projectsGrid.getChildren().add(buildProjectCard(p));
        }
        if (!showingArchived && searchQuery.isEmpty()) {
            projectsGrid.getChildren().add(buildNewProjectCard());
        }
    }

    private void updateStatCards() {
        List<Project> active = projectService.getActiveProjects();
        int count = active.size();
        double total = 0;
        for (Project p : active) total += progressService.calculateProgressFraction(p.getId());
        int avgPct = count > 0 ? (int) ((total / count) * 100) : 0;

        if (statActiveProjects    != null) statActiveProjects.setText(String.valueOf(count));
        if (statTotalCompletion   != null) statTotalCompletion.setText(avgPct + "%");
        if (statUpcomingDeadlines != null) statUpcomingDeadlines.setText("—");
    }

    private VBox buildProjectCard(Project project) {
        VBox card = new VBox(10);
        card.setPrefWidth(310);
        card.setMaxWidth(310);
        card.getStyleClass().add("project-card");
        if (project.isArchived()) card.getStyleClass().add("project-card-archived");

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_RIGHT);
        Label stageBadge = new Label(project.getCurrentStage());
        stageBadge.getStyleClass().add("project-card-stage-badge");
        topRow.getChildren().add(stageBadge);

        Label nameLabel = new Label(project.getName());
        nameLabel.getStyleClass().add("project-card-title");
        nameLabel.setWrapText(true);

        String desc = (project.getDescription() != null && !project.getDescription().isBlank())
                ? project.getDescription() : "No description";
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("project-card-desc");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(36);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Region divider = new Region();
        divider.getStyleClass().add("divider-line");
        divider.setMaxWidth(Double.MAX_VALUE);

        double fraction = progressService.calculateProgressFraction(project.getId());
        int pct = (int) (fraction * 100);

        HBox progressHeader = new HBox();
        progressHeader.setAlignment(Pos.CENTER_LEFT);
        Label metaLabel = new Label("Progress Matrix");
        metaLabel.getStyleClass().add("project-card-meta-label");
        Region ph = new Region();
        HBox.setHgrow(ph, Priority.ALWAYS);
        Label pctLabel = new Label(pct + "%");
        pctLabel.getStyleClass().add("project-card-pct");
        progressHeader.getChildren().addAll(metaLabel, ph, pctLabel);

        ProgressBar bar = new ProgressBar(fraction);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.getStyleClass().clear();
        bar.getStyleClass().add("progress-bar");

        HBox actionRow = new HBox();
        actionRow.setAlignment(Pos.CENTER_RIGHT);
        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("btn-icon");
        deleteBtn.setStyle("-fx-font-size:10px; -fx-text-fill:#3D3D3D; -fx-padding:3 7 3 7;");
        deleteBtn.setOnAction(e -> { e.consume(); handleDeleteProject(project); });
        actionRow.getChildren().add(deleteBtn);

        card.getChildren().addAll(topRow, nameLabel, descLabel,
                spacer, divider, progressHeader, bar, actionRow);

        // FIX: WindowManager uses static methods — no getInstance()
        card.setOnMouseClicked(e -> {
            if (e.getTarget() != deleteBtn) {
                WindowManager.showProjectDetail(project.getId());
            }
        });
        return card;
    }

    private VBox buildNewProjectCard() {
        VBox card = new VBox(8);
        card.setPrefWidth(310);
        card.setMaxWidth(310);
        card.setMinHeight(160);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("project-card");
        card.setStyle("-fx-background-color:rgba(255,255,255,0.02);" +
                "-fx-border-color:rgba(255,255,255,0.05);-fx-border-style:dashed;-fx-cursor:hand;");

        Label plus = new Label("+");
        plus.setStyle("-fx-font-size:26px; -fx-text-fill:#333333;");
        Label lbl  = new Label("New Project");
        lbl.setStyle("-fx-font-size:13px; -fx-text-fill:#3D3D3D; -fx-font-weight:bold;");
        Label sub  = new Label("Begin a new lifecycle");
        sub.setStyle("-fx-font-size:11px; -fx-text-fill:#2A2A2A;");

        card.getChildren().addAll(plus, lbl, sub);
        card.setOnMouseClicked(e -> handleCreateProject());
        return card;
    }

    @FXML
    public void handleCreateProject() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Project");
        dialog.setHeaderText("Create Project");

        DialogPane dp = dialog.getDialogPane();
        dp.getStylesheets().add(getClass().getResource("/css/obsidian.css").toExternalForm());
        dp.getStyleClass().add("dialog-pane");

        ButtonType createBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dp.getButtonTypes().addAll(createBtn, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(16, 0, 4, 0));

        TextField nameField = new TextField();
        nameField.setPromptText("Project name");
        nameField.getStyleClass().add("text-field");
        TextField descField = new TextField();
        descField.setPromptText("Short description (optional)");
        descField.getStyleClass().add("text-field");

        Label lName = new Label("Name");
        lName.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");
        Label lDesc = new Label("Description");
        lDesc.setStyle("-fx-text-fill:#888; -fx-font-size:11px;");

        content.getChildren().addAll(lName, nameField, lDesc, descField);
        dp.setContent(content);
        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(btn -> btn == createBtn ? nameField.getText().trim() : null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.isEmpty()) {
                projectService.createProject(name, descField.getText().trim());
                loadProjects();
                setStatus("Created: " + name);
            }
        });
    }

    private void handleDeleteProject(Project project) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Project");
        confirm.setHeaderText("Delete \"" + project.getName() + "\"?");
        confirm.setContentText("All stages and tasks will be permanently removed.");
        confirm.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/obsidian.css").toExternalForm());
        confirm.initOwner(WindowManager.getPrimaryStage());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            projectService.deleteProject(project.getId());
            loadProjects();
            setStatus("Deleted: " + project.getName());
        }
    }

    @FXML
    public void handleShowArchived() {
        showingArchived = !showingArchived;
        if (dashboardTitleLabel != null)
            dashboardTitleLabel.setText(showingArchived ? "Archived Projects" : "Active Projects");
        showArchivedBtn.setText(showingArchived ? "← Active" : "🗄 Archived");
        if (showingArchived) showArchivedBtn.getStyleClass().add("btn-archive-toggle");
        else                 showArchivedBtn.getStyleClass().remove("btn-archive-toggle");
        loadProjects();
    }

    @FXML
    public void handleThemeToggle() {
        ThemeManager.toggle();
        ThemeManager.applyCurrentTheme();
        themeToggleBtn.setText(ThemeManager.getToggleLabel());
    }

    private void setStatus(String message) {
        if (statusLabel != null) statusLabel.setText(message);
    }
}