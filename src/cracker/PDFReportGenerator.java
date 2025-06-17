package cracker;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;

public class PDFReportGenerator {

    public static void generate(
            String timestamp,
            String seqResult, double seqAvgTime, double seqAvgMem, double seqAvgCPUPercent,
            String recParResult, double recParAvgTime, double recParAvgMem, double recParAvgCPUPercent, double recSpeedup,
            String fullParResult, double fullParAvgTime, double fullParAvgMem, double fullParAvgCPUPercent,
            double fullSpeedup, int fullParThreads
    ) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("CrackReport.pdf"));
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLACK);
            Paragraph title = new Paragraph("Password Cracker Benchmark Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Timestamp
            Font timeFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph time = new Paragraph("Generated on: " + timestamp, timeFont);
            time.setAlignment(Element.ALIGN_RIGHT);
            time.setSpacingAfter(15);
            document.add(time);

            // Table setup
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Header
            String[] headers = {"Method", "Password Found", "Avg Time (s)", "Avg Mem (MB)", "Avg CPU (%)"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Sequential
            table.addCell(new Phrase("Sequential", cellFont));
            table.addCell(new Phrase(seqResult, cellFont));
            table.addCell(new Phrase(String.format("%.3f", seqAvgTime), cellFont));
            table.addCell(new Phrase(String.format("%.2f", seqAvgMem), cellFont));
            table.addCell(new Phrase(String.format("%.2f", seqAvgCPUPercent), cellFont));

            // Recursive Parallel
            table.addCell(new Phrase("Recursive Parallel", cellFont));
            table.addCell(new Phrase(recParResult, cellFont));
            table.addCell(new Phrase(String.format("%.3f", recParAvgTime), cellFont));
            table.addCell(new Phrase(String.format("%.2f", recParAvgMem), cellFont));
            table.addCell(new Phrase(String.format("%.2f", recParAvgCPUPercent), cellFont));

            // Full Parallel
            table.addCell(new Phrase("Full Parallel", cellFont));
            table.addCell(new Phrase(fullParResult + " (Threads: " + fullParThreads + ")", cellFont));
            table.addCell(new Phrase(String.format("%.3f", fullParAvgTime), cellFont));
            table.addCell(new Phrase(String.format("%.2f", fullParAvgMem), cellFont));
            table.addCell(new Phrase(String.format("%.2f", fullParAvgCPUPercent), cellFont));

            document.add(table);

            // Speed-up section
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph speedupTitle = new Paragraph("Speed-up Summary", subTitleFont);
            speedupTitle.setSpacingBefore(10);
            speedupTitle.setSpacingAfter(10);
            document.add(speedupTitle);

            Paragraph speedupText = new Paragraph(
                    String.format("Recursive Parallel Speed-up: %.2fx%nFull Parallel Speed-up: %.2fx",
                            recSpeedup, fullSpeedup),
                    cellFont
            );
            speedupText.setSpacingAfter(10);
            document.add(speedupText);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
