package com.mint.pab.controller;

import com.mint.pab.dto.PageResult;
import com.mint.pab.dto.Result;
import com.mint.pab.dto.TransactionDTO;
import com.mint.pab.dto.TransactionQueryDTO;
import com.mint.pab.service.TransactionService;
import com.mint.pab.vo.TransactionVO;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public Result<TransactionVO> create(@Valid @RequestBody TransactionDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TransactionVO vo = transactionService.createTransaction(userId, dto);
        return Result.success(vo);
    }

    @PutMapping("/{id}")
    public Result<TransactionVO> update(@PathVariable Long id, @Valid @RequestBody TransactionDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TransactionVO vo = transactionService.updateTransaction(userId, id, dto);
        return Result.success(vo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        transactionService.deleteTransaction(userId, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<TransactionVO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TransactionVO vo = transactionService.getTransaction(userId, id);
        return Result.success(vo);
    }

    @GetMapping
    public Result<PageResult<TransactionVO>> list(TransactionQueryDTO query, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PageResult<TransactionVO> result = transactionService.queryTransactions(userId, query);
        return Result.success(result);
    }

    @GetMapping("/export")
    public void export(TransactionQueryDTO query, @RequestParam String format,
                       HttpServletRequest request, HttpServletResponse response) {
        Long userId = (Long) request.getAttribute("userId");
        List<TransactionVO> list = transactionService.queryAllTransactions(userId, query);

        try {
            if ("excel".equalsIgnoreCase(format)) {
                exportExcel(list, response);
            } else if ("pdf".equalsIgnoreCase(format)) {
                exportPdf(list, response);
            } else {
                throw new IllegalArgumentException("不支持的导出格式: " + format);
            }
        } catch (Exception e) {
            log.error("导出失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

    private void exportExcel(List<TransactionVO> list, HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("交易流水.xlsx", "UTF-8"));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("交易流水");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = {"ID", "类型", "转出账户", "转入账户", "金额", "一级分类", "二级分类", "交易时间", "备注"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (TransactionVO vo : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(vo.getId() != null ? vo.getId() : 0L);
            row.createCell(1).setCellValue(vo.getTypeName());
            row.createCell(2).setCellValue(vo.getFromAccountName());
            row.createCell(3).setCellValue(vo.getToAccountName());
            row.createCell(4).setCellValue(vo.getAmount() != null ? vo.getAmount().doubleValue() : 0);
            row.createCell(5).setCellValue(vo.getCategoryParentName());
            row.createCell(6).setCellValue(vo.getCategoryName());
            row.createCell(7).setCellValue(vo.getTransactionTime());
            row.createCell(8).setCellValue(vo.getRemark());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (OutputStream out = response.getOutputStream()) {
            workbook.write(out);
        }
        workbook.close();
    }

    private void exportPdf(List<TransactionVO> list, HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode("交易流水.pdf", "UTF-8"));

        BaseFont baseFont;
        try {
            baseFont = BaseFont.createFont("C:/Windows/Fonts/simhei.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            try {
                baseFont = BaseFont.createFont("/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc,0", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            } catch (Exception ex) {
                baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            }
        }

        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(baseFont, 16, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(baseFont, 10, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font cellFont = new com.itextpdf.text.Font(baseFont, 9, com.itextpdf.text.Font.NORMAL);

        Document document = new Document();
        try (OutputStream out = response.getOutputStream()) {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("交易流水", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 1.2f, 1.5f, 1.5f, 1.2f, 1.5f, 1.5f, 2, 2});

            String[] headers = {"ID", "类型", "转出账户", "转入账户", "金额", "一级分类", "二级分类", "交易时间", "备注"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            for (TransactionVO vo : list) {
                addPdfCell(table, vo.getId() != null ? String.valueOf(vo.getId()) : "", cellFont);
                addPdfCell(table, vo.getTypeName(), cellFont);
                addPdfCell(table, vo.getFromAccountName(), cellFont);
                addPdfCell(table, vo.getToAccountName(), cellFont);
                addPdfCell(table, vo.getAmount() != null ? vo.getAmount().toString() : "", cellFont);
                addPdfCell(table, vo.getCategoryParentName(), cellFont);
                addPdfCell(table, vo.getCategoryName(), cellFont);
                addPdfCell(table, vo.getTransactionTime(), cellFont);
                addPdfCell(table, vo.getRemark(), cellFont);
            }

            document.add(table);
            document.close();
        }
    }

    private void addPdfCell(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}
