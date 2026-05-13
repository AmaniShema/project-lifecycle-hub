package com.lifecyclehub.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.lifecyclehub.entity.Project;
import com.lifecyclehub.entity.Stage;
import com.lifecyclehub.entity.Task;
import com.lifecyclehub.repository.StageRepository;
import com.lifecyclehub.repository.TaskRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PdfExportService — professional project report.
 * Greyscale Obsidian aesthetic. Strict stage/task hierarchy.
 * v2.2.1 — fixed iText5 "Element not allowed" error.
 */
public class PdfExportService {

    // ── Greyscale Palette ─────────────────────────────────────
    private static final BaseColor C_BLACK    = new BaseColor(0x0A, 0x0A, 0x0A);
    private static final BaseColor C_DARK     = new BaseColor(0x1E, 0x1E, 0x2E);
    private static final BaseColor C_MID      = new BaseColor(0x44, 0x44, 0x55);
    private static final BaseColor C_SUBTLE   = new BaseColor(0x6C, 0x70, 0x86);
    private static final BaseColor C_BORDER   = new BaseColor(0xC0, 0xC4, 0xD0);
    private static final BaseColor C_LIGHT    = new BaseColor(0xF4, 0xF5, 0xF7);
    private static final BaseColor C_STAGE    = new BaseColor(0x1E, 0x1E, 0x2E);
    private static final BaseColor C_WHITE    = BaseColor.WHITE;
    private static final BaseColor C_ACCENT   = new BaseColor(0xB4, 0xBE, 0xFE);
    private static final BaseColor C_HIGH_BG  = new BaseColor(0xFF, 0xF2, 0xF4);
    private static final BaseColor C_MED_BG   = new BaseColor(0xFF, 0xF7, 0xF0);
    private static final BaseColor C_LOW_BG   = new BaseColor(0xF2, 0xF5, 0xFF);

    // ── Fonts ─────────────────────────────────────────────────
    private static final Font F_COVER_TITLE = new Font(Font.FontFamily.HELVETICA, 26, Font.BOLD,   C_WHITE);
    private static final Font F_COVER_SUB   = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, C_ACCENT);
    private static final Font F_STAGE_TITLE = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD,   C_WHITE);
    private static final Font F_STAGE_SUB   = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, new BaseColor(0xA0,0xA4,0xB8));
    private static final Font F_SECTION_LBL = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   C_SUBTLE);
    private static final Font F_TASK        = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   C_DARK);
    private static final Font F_TASK_DONE   = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, C_BORDER);
    private static final Font F_TASK_NOTES  = new Font(Font.FontFamily.HELVETICA,  9, Font.ITALIC, C_MID);
    private static final Font F_META        = new Font(Font.FontFamily.HELVETICA,  9, Font.NORMAL, C_SUBTLE);
    private static final Font F_META_BOLD   = new Font(Font.FontFamily.HELVETICA,  9, Font.BOLD,   C_MID);
    private static final Font F_BODY        = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, C_DARK);
    private static final Font F_BODY_BOLD   = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   C_DARK);
    private static final Font F_OVERVIEW    = new Font(Font.FontFamily.HELVETICA,  9, Font.ITALIC, C_MID);
    private static final Font F_SMALL       = new Font(Font.FontFamily.HELVETICA,  8, Font.NORMAL, C_SUBTLE);
    private static final Font F_OVERDUE     = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   new BaseColor(0xB0,0x30,0x40));
    private static final Font F_DUE_SOON    = new Font(Font.FontFamily.HELVETICA,  8, Font.BOLD,   new BaseColor(0x99,0x70,0x20));

    private static final DateTimeFormatter FMT_DISPLAY = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter FMT_FILE    = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final StageRepository stageRepository = new StageRepository();
    private final TaskRepository  taskRepository  = new TaskRepository();
    private final ProgressService progressService = new ProgressService();

    // ── Entry Point ───────────────────────────────────────────

    public File exportProject(Project project) throws IOException, DocumentException {
        String docsDir = System.getProperty("user.home") + "/Documents";
        new File(docsDir).mkdirs();
        String safe     = project.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String fileName = safe + "_Report_" + LocalDate.now().format(FMT_FILE) + ".pdf";
        String fullPath = docsDir + "/" + fileName;

        System.out.println("[PdfExport] Generating: " + fullPath);

        Document doc = new Document(PageSize.A4, 45, 45, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(fullPath));
        writer.setPageEvent(new FooterEvent(project.getName()));
        doc.open();

        addCoverPage(doc, project);
        doc.newPage();
        addProjectSummary(doc, project);

        List<Stage> stages = stageRepository.findByProjectId(project.getId());
        for (Stage stage : stages) {
            doc.newPage();
            addStagePage(doc, stage, taskRepository.findByStageId(stage.getId()),
                project.getCurrentStage());
        }

        doc.close();
        System.out.println("[PdfExport] Complete: " + fullPath);
        return new File(fullPath);
    }

    // ── Cover Page ────────────────────────────────────────────

    private void addCoverPage(Document doc, Project project) throws DocumentException {
        PdfPTable cover = new PdfPTable(1);
        cover.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(C_STAGE);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(30);

        Paragraph appLbl = new Paragraph("PROJECT LIFE-CYCLE HUB", F_COVER_SUB);
        appLbl.setSpacingAfter(10);
        cell.addElement(appLbl);

        Paragraph titleP = new Paragraph(project.getName(), F_COVER_TITLE);
        titleP.setSpacingAfter(8);
        cell.addElement(titleP);

        if (project.getDescription() != null && !project.getDescription().isBlank()) {
            Paragraph descP = new Paragraph(project.getDescription(), F_COVER_SUB);
            descP.setSpacingAfter(20);
            cell.addElement(descP);
        }

        cell.addElement(new Chunk(new LineSeparator(0.5f, 100, C_MID, Element.ALIGN_LEFT, -2)));

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingBefore(8);
        cell.addElement(spacer);

        Paragraph meta = new Paragraph();
        meta.add(new Chunk("Stage:  ", F_SECTION_LBL));
        meta.add(new Chunk(project.getCurrentStage() + "     ", F_STAGE_SUB));
        meta.add(new Chunk("Generated:  ", F_SECTION_LBL));
        meta.add(new Chunk(LocalDate.now().format(FMT_DISPLAY), F_STAGE_SUB));
        cell.addElement(meta);

        cover.addCell(cell);
        doc.add(cover);
    }

    // ── Project Summary ───────────────────────────────────────

    private void addProjectSummary(Document doc, Project project) throws DocumentException {
        addSectionLabel(doc, "PROJECT SUMMARY");
        addVerticalSpace(doc, 6);

        PdfPTable info = new PdfPTable(new float[]{1f, 2f});
        info.setWidthPercentage(100);
        info.setSpacingAfter(14);
        addInfoRow(info, "Project",        project.getName());
        addInfoRow(info, "Current Stage",  project.getCurrentStage());
        addInfoRow(info, "Created",
            project.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        addInfoRow(info, "Report Date",    LocalDate.now().format(FMT_DISPLAY));
        addInfoRow(info, "Status",         project.isArchived() ? "Archived" : "Active");
        doc.add(info);

        addSectionLabel(doc, "OVERALL PROGRESS");
        addVerticalSpace(doc, 4);

        double progress = progressService.calculateProgress(project.getId());
        int    pct      = (int) Math.round(progress);
        String summary  = progressService.getTaskSummary(project.getId());
        addProgressBar(doc, progress, pct + "%  —  " + summary);
        addVerticalSpace(doc, 10);

        addSectionLabel(doc, "STAGE BREAKDOWN");
        addVerticalSpace(doc, 4);

        PdfPTable stageTable = new PdfPTable(new float[]{3f, 1f, 1f, 1f});
        stageTable.setWidthPercentage(100);
        stageTable.setSpacingAfter(10);

        for (String h : new String[]{"Stage", "Total", "Done", "%"}) {
            PdfPCell hc = new PdfPCell(new Phrase(h, F_META_BOLD));
            hc.setBackgroundColor(C_LIGHT); hc.setBorderColor(C_BORDER); hc.setPadding(6);
            stageTable.addCell(hc);
        }

        List<Stage> stages = stageRepository.findByProjectId(project.getId());
        for (Stage stage : stages) {
            List<Task> tasks = taskRepository.findByStageId(stage.getId());
            int  total    = tasks.size();
            long done     = tasks.stream().filter(Task::isCompleted).count();
            int  stagePct = total > 0 ? (int)((done / (double) total) * 100) : 0;
            boolean cur   = stage.getName().equals(project.getCurrentStage());

            PdfPCell nc = new PdfPCell(new Phrase(
                (cur ? "▶ " : "  ") + stage.getName(), cur ? F_BODY_BOLD : F_BODY));
            nc.setBorderColor(C_BORDER); nc.setPadding(6);
            if (cur) nc.setBackgroundColor(C_LIGHT);
            stageTable.addCell(nc);

            PdfPCell tc = new PdfPCell(new Phrase(String.valueOf(total), F_META));
            tc.setBorderColor(C_BORDER); tc.setPadding(6);
            tc.setHorizontalAlignment(Element.ALIGN_CENTER);
            stageTable.addCell(tc);

            PdfPCell dc = new PdfPCell(new Phrase(String.valueOf(done), F_META));
            dc.setBorderColor(C_BORDER); dc.setPadding(6);
            dc.setHorizontalAlignment(Element.ALIGN_CENTER);
            stageTable.addCell(dc);

            PdfPCell pc = new PdfPCell(new Phrase(stagePct + "%", F_META_BOLD));
            pc.setBorderColor(C_BORDER); pc.setPadding(6);
            pc.setHorizontalAlignment(Element.ALIGN_CENTER);
            stageTable.addCell(pc);
        }
        doc.add(stageTable);
    }

    // ── Stage Page ────────────────────────────────────────────

    private void addStagePage(Document doc, Stage stage,
            List<Task> tasks, String currentStage) throws DocumentException {

        // Stage header block
        PdfPTable stageHeader = new PdfPTable(1);
        stageHeader.setWidthPercentage(100);
        stageHeader.setSpacingAfter(10);

        PdfPCell hc = new PdfPCell();
        hc.setBackgroundColor(C_STAGE);
        hc.setBorder(Rectangle.NO_BORDER);
        hc.setPaddingTop(14); hc.setPaddingBottom(14);
        hc.setPaddingLeft(16); hc.setPaddingRight(16);

        boolean isCurrent = stage.getName().equals(currentStage);
        String stageLine  = stage.getName() + (isCurrent ? "   ◀ CURRENT STAGE" : "");
        Paragraph stageP = new Paragraph(stageLine, F_STAGE_TITLE);
        stageP.setSpacingAfter(4);
        hc.addElement(stageP);

        long done  = tasks.stream().filter(Task::isCompleted).count();
        int  total = tasks.size();
        hc.addElement(new Paragraph(total + " tasks  •  " + done + " completed", F_STAGE_SUB));
        stageHeader.addCell(hc);
        doc.add(stageHeader);

        // Stage progress bar
        double frac = total > 0 ? (double) done / total : 0.0;
        addProgressBar(doc, frac * 100, (int) Math.round(frac * 100) + "% complete");
        addVerticalSpace(doc, 8);

        // Phase Overview (stage notes)
        if (stage.getNotes() != null && !stage.getNotes().isBlank()) {
            addSectionLabel(doc, "PHASE OVERVIEW");
            addVerticalSpace(doc, 4);

            PdfPTable ov = new PdfPTable(1);
            ov.setWidthPercentage(100);
            ov.setSpacingAfter(12);
            PdfPCell ovc = new PdfPCell(new Phrase(stage.getNotes(), F_OVERVIEW));
            ovc.setBackgroundColor(C_LIGHT);
            ovc.setBorderColor(C_BORDER);
            ovc.setPadding(10);
            ov.addCell(ovc);
            doc.add(ov);
        }

        // Tasks
        addSectionLabel(doc, "TASKS");
        addVerticalSpace(doc, 6);

        if (tasks.isEmpty()) {
            doc.add(new Paragraph("No tasks in this stage.", F_META));
            return;
        }

        for (int i = 0; i < tasks.size(); i++) {
            addTaskBlock(doc, tasks.get(i));
            if (i < tasks.size() - 1) {
                addVerticalSpace(doc, 2);
                doc.add(new Chunk(new LineSeparator(0.4f, 100, C_BORDER, Element.ALIGN_LEFT, -2)));
                addVerticalSpace(doc, 4);
            }
        }
    }

    // ── Task Block ────────────────────────────────────────────

    private void addTaskBlock(Document doc, Task task) throws DocumentException {
        boolean done = task.isCompleted();

        BaseColor rowBg = switch (task.getPriority()) {
            case HIGH   -> done ? C_WHITE : C_HIGH_BG;
            case MEDIUM -> done ? C_WHITE : C_MED_BG;
            case LOW    -> done ? C_WHITE : C_LOW_BG;
            default     -> C_WHITE;
        };

        // Three-column row: [icon] [title + notes] [priority + due]
        PdfPTable row = new PdfPTable(new float[]{0.5f, 5f, 2f});
        row.setWidthPercentage(100);
        row.setSpacingBefore(4);
        row.setSpacingAfter(2);

        // Status icon
        String icon      = done ? "+" : "o";
        Font   iconFont  = done
            ? new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   C_BORDER)
            : new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, C_SUBTLE);
        PdfPCell iconCell = new PdfPCell(new Phrase(icon, iconFont));
        iconCell.setBorder(Rectangle.NO_BORDER);
        iconCell.setBackgroundColor(rowBg);
        iconCell.setPaddingTop(6); iconCell.setPaddingBottom(4); iconCell.setPaddingLeft(6);
        iconCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        iconCell.setVerticalAlignment(Element.ALIGN_TOP);
        row.addCell(iconCell);

        // Title + task notes
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setBackgroundColor(rowBg);
        titleCell.setPaddingTop(5); titleCell.setPaddingBottom(4); titleCell.setPaddingLeft(4);
        titleCell.setVerticalAlignment(Element.ALIGN_TOP);

        Paragraph titleP = new Paragraph(task.getTitle(), done ? F_TASK_DONE : F_TASK);
        titleP.setSpacingAfter(0);
        titleCell.addElement(titleP);

        if (task.hasNotes()) {
            Paragraph notesP = new Paragraph(task.getNotes(), F_TASK_NOTES);
            notesP.setIndentationLeft(10);
            notesP.setSpacingBefore(3);
            titleCell.addElement(notesP);
        }
        row.addCell(titleCell);

        // Priority + due date
        PdfPCell metaCell = new PdfPCell();
        metaCell.setBorder(Rectangle.NO_BORDER);
        metaCell.setBackgroundColor(rowBg);
        metaCell.setPaddingTop(5); metaCell.setPaddingBottom(4); metaCell.setPaddingRight(6);
        metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaCell.setVerticalAlignment(Element.ALIGN_TOP);

        if (task.getPriority() != Task.Priority.NONE) {
            String plabel = switch (task.getPriority()) {
                case HIGH   -> "! HIGH";
                case MEDIUM -> "~ MED";
                case LOW    -> "V LOW";
                default     -> "";
            };
            Paragraph pp = new Paragraph(plabel, F_META_BOLD);
            pp.setAlignment(Element.ALIGN_RIGHT);
            pp.setSpacingAfter(2);
            metaCell.addElement(pp);
        }

        if (task.getDueDate() != null) {
            String dateStr = task.getDueDate().format(FMT_DISPLAY);
            Font   dueFont;
            String prefix;
            if (done) {
                prefix = "Done"; dueFont = F_SMALL;
            } else if (task.isOverdue()) {
                prefix = "OVERDUE"; dueFont = F_OVERDUE;
            } else if (task.isDueSoon()) {
                prefix = "Soon"; dueFont = F_DUE_SOON;
            } else {
                prefix = "Due"; dueFont = F_SMALL;
            }
            Paragraph dp = new Paragraph(prefix + " " + dateStr, dueFont);
            dp.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(dp);
        }
        row.addCell(metaCell);
        doc.add(row);
    }

    // ── Progress Bar ──────────────────────────────────────────

    /**
     * Draws a greyscale progress bar as a two-row table.
     * FIX: Does NOT use nested PdfPTable inside PdfPCell (iText5 restriction).
     * Instead uses a two-cell horizontal table at document level.
     */
    private void addProgressBar(Document doc, double progressPct, String label)
            throws DocumentException {

        double pct = Math.max(0, Math.min(100, progressPct));

        // Label row
        Paragraph labelP = new Paragraph(label, F_META_BOLD);
        labelP.setSpacingBefore(2);
        labelP.setSpacingAfter(3);
        doc.add(labelP);

        // Bar row — two cells: filled | empty
        // Use a table with NO_BORDER at document level (not inside a cell)
        if (pct <= 0) {
            // Empty bar only
            PdfPTable bar = new PdfPTable(1);
            bar.setWidthPercentage(100);
            bar.setSpacingAfter(8);
            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBackgroundColor(C_BORDER);
            empty.setFixedHeight(6);
            empty.setBorder(Rectangle.NO_BORDER);
            bar.addCell(empty);
            doc.add(bar);
        } else if (pct >= 100) {
            // Full bar
            PdfPTable bar = new PdfPTable(1);
            bar.setWidthPercentage(100);
            bar.setSpacingAfter(8);
            PdfPCell filled = new PdfPCell(new Phrase(""));
            filled.setBackgroundColor(C_DARK);
            filled.setFixedHeight(6);
            filled.setBorder(Rectangle.NO_BORDER);
            bar.addCell(filled);
            doc.add(bar);
        } else {
            // Split bar
            PdfPTable bar = new PdfPTable(new float[]{(float) pct, (float)(100 - pct)});
            bar.setWidthPercentage(100);
            bar.setSpacingAfter(8);

            PdfPCell filled = new PdfPCell(new Phrase(""));
            filled.setBackgroundColor(C_DARK);
            filled.setFixedHeight(6);
            filled.setBorder(Rectangle.NO_BORDER);

            PdfPCell empty = new PdfPCell(new Phrase(""));
            empty.setBackgroundColor(C_BORDER);
            empty.setFixedHeight(6);
            empty.setBorder(Rectangle.NO_BORDER);

            bar.addCell(filled);
            bar.addCell(empty);
            doc.add(bar);
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private void addSectionLabel(Document doc, String text) throws DocumentException {
        Paragraph p = new Paragraph(text, F_SECTION_LBL);
        p.setSpacingBefore(2);
        p.setSpacingAfter(2);
        doc.add(p);
    }

    private void addVerticalSpace(Document doc, float height) throws DocumentException {
        Paragraph sp = new Paragraph(" ");
        sp.setLeading(height);
        doc.add(sp);
    }

    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell lc = new PdfPCell(new Phrase(label, F_META_BOLD));
        lc.setBackgroundColor(C_LIGHT); lc.setBorderColor(C_BORDER); lc.setPadding(7);
        table.addCell(lc);
        PdfPCell vc = new PdfPCell(new Phrase(value, F_BODY));
        vc.setBorderColor(C_BORDER); vc.setPadding(7);
        table.addCell(vc);
    }

    // ── Footer Event ──────────────────────────────────────────

    static class FooterEvent extends PdfPageEventHelper {
        private final String projectName;
        FooterEvent(String n) { this.projectName = n; }

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContent();
            cb.setColorStroke(new BaseColor(0xC0, 0xC4, 0xD0));
            cb.setLineWidth(0.4f);
            cb.moveTo(doc.left(), doc.bottom() - 8);
            cb.lineTo(doc.right(), doc.bottom() - 8);
            cb.stroke();

            Font f = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL,
                new BaseColor(0x9A, 0x9E, 0xB2));

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase(projectName + "  -  Life-Cycle Hub Report", f),
                doc.left(), doc.bottom() - 18, 0);

            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase("Page " + writer.getPageNumber(), f),
                doc.right(), doc.bottom() - 18, 0);
        }
    }
}
