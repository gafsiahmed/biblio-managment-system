package com.bibliotheque.service;

import com.bibliotheque.model.Loan;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.LoanStatus;
import com.bibliotheque.repository.LoanRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final LoanRepository loanRepository;

    public List<Loan> generateLoanReport(LocalDateTime start, LocalDateTime end) {
        return loanRepository.findByLoanDateBetween(start, end);
    }

    public List<Object[]> generateUserActivityReport() {
        return loanRepository.findMostActiveUsers(org.springframework.data.domain.PageRequest.of(0, 100));
    }

    public List<Object[]> generateResourceUsageReport() {
        return loanRepository.findTopBorrowedResources(org.springframework.data.domain.PageRequest.of(0, 100));
    }

    public void exportUserActivityToCSV(HttpServletResponse response) throws IOException {
        List<Object[]> data = generateUserActivityReport();
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=user_activity.csv");

        try (PrintWriter writer = response.getWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("User", "Loans Count"))) {
            for (Object[] row : data) {
                User user = (User) row[0];
                Long count = (Long) row[1];
                csvPrinter.printRecord(user.getUsername(), count);
            }
        }
    }

    public void exportResourceUsageToCSV(HttpServletResponse response) throws IOException {
        List<Object[]> data = generateResourceUsageReport();
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=resource_usage.csv");

        try (PrintWriter writer = response.getWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Resource", "Loans Count"))) {
            for (Object[] row : data) {
                Resource resource = (Resource) row[0];
                Long count = (Long) row[1];
                csvPrinter.printRecord(resource.getTitle(), count);
            }
        }
    }

    public void exportLoansToCSV(HttpServletResponse response, LocalDateTime start, LocalDateTime end) throws IOException {
        List<Loan> loans = generateLoanReport(start, end);
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=loans_report.csv");

        try (PrintWriter writer = response.getWriter();
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("ID", "User", "Resource", "Loan Date", "Due Date", "Status"))) {
            for (Loan loan : loans) {
                csvPrinter.printRecord(loan.getId(), loan.getUser().getUsername(), loan.getResource().getTitle(), loan.getLoanDate(), loan.getDueDate(), loan.getStatus());
            }
        }
    }

    public void exportLoansToPDF(HttpServletResponse response, LocalDateTime start, LocalDateTime end) throws IOException {
        List<Loan> loans = generateLoanReport(start, end);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=loans_report.pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);
        font.setColor(java.awt.Color.BLUE);

        Paragraph p = new Paragraph("Rapport des Prêts", font);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] {2.0f, 3.5f, 2.5f, 2.5f, 2.0f});
        table.setSpacingBefore(10);

        writeTableHeader(table);
        writeTableData(table, loans);

        document.add(table);
        document.close();
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(java.awt.Color.BLUE);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(java.awt.Color.WHITE);

        cell.setPhrase(new Phrase("User", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Resource", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Loan Date", font));
        table.addCell(cell);
        
        cell.setPhrase(new Phrase("Due Date", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Status", font));
        table.addCell(cell);
    }

    private void writeTableData(PdfPTable table, List<Loan> loans) {
        for (Loan loan : loans) {
            table.addCell(loan.getUser().getUsername());
            table.addCell(loan.getResource().getTitle());
            table.addCell(loan.getLoanDate() != null ? loan.getLoanDate().toLocalDate().toString() : "");
            table.addCell(loan.getDueDate() != null ? loan.getDueDate().toLocalDate().toString() : "");
            table.addCell(String.valueOf(loan.getStatus()));
        }
    }
}
