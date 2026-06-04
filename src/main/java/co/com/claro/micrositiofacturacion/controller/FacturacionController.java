package co.com.claro.micrositiofacturacion.controller;

import co.com.claro.micrositiofacturacion.dto.AcifBaseActasDTO;
import co.com.claro.micrositiofacturacion.dto.AcifSerialesDTO;
import co.com.claro.micrositiofacturacion.dto.PageResponseDTO;
import co.com.claro.micrositiofacturacion.service.AcifService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@RestController
@RequestMapping("api/facturacion")
public class FacturacionController {

    private static final DateTimeFormatter CSV_FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final AcifService acifService;

    public FacturacionController(AcifService acifService) {
        this.acifService = acifService;
    }

    @GetMapping("/acif/base-actas/{idCargueFk}")
    public ResponseEntity<PageResponseDTO<AcifBaseActasDTO>> findBaseActasByIdCargue(
            @PathVariable("idCargueFk") Long idCargueFk,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size) {
        return ResponseEntity.ok(acifService.findBaseActasByIdCargue(idCargueFk, page, size));
    }

    @GetMapping("/acif/seriales/{idCargueFk}")
    public ResponseEntity<PageResponseDTO<AcifSerialesDTO>> findAcifSerialByIdCargue(
            @PathVariable("idCargueFk") Long idCargueFk,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "100") Integer size) {
        return ResponseEntity.ok(acifService.findSerialesByIdCargue(idCargueFk, page, size));
    }

    @GetMapping("/acif/base-actas/{idCargueFk}/csv")
    public ResponseEntity<StreamingResponseBody> generateBaseActasCsv(
            @PathVariable("idCargueFk") Long idCargueFk) {
        return csvResponse(
                "acif-base-actas-" + idCargueFk,
                acifService.generateBaseActasCsv(idCargueFk));
    }

    @GetMapping("/acif/seriales/{idCargueFk}/csv")
    public ResponseEntity<StreamingResponseBody> generateSerialesCsv(
            @PathVariable("idCargueFk") Long idCargueFk) {
        return csvResponse(
                "acif-seriales-" + idCargueFk,
                acifService.generateSerialesCsv(idCargueFk));
    }

    private ResponseEntity<StreamingResponseBody> csvResponse(
            String filePrefix,
            StreamingResponseBody csvStream) {
        String fileName = filePrefix + "-" + LocalDateTime.now().format(CSV_FILE_TIMESTAMP) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csvStream);
    }
}
