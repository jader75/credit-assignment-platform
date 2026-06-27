package br.com.srm.credit.infrastructure.web.batch;

import br.com.srm.credit.application.batch.BatchImportApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/batches")
@Tag(name = "Batches", description = "Importacao de lotes de recebiveis")
public class BatchImportController {

    private final BatchImportApplicationService batchImportApplicationService;

    public BatchImportController(BatchImportApplicationService batchImportApplicationService) {
        this.batchImportApplicationService = batchImportApplicationService;
    }

    @PostMapping(value = "/imports", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar lote de recebiveis")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Lote importado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Arquivo ou parametros invalidos"),
        @ApiResponse(responseCode = "422", description = "Regra de negocio violada")
    })
    public ResponseEntity<BatchImportResponse> importBatch(
            @RequestParam String batchReference,
            @Parameter(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestPart("file")
                    MultipartFile file)
            throws IOException {
        var result = batchImportApplicationService.importBatch(batchReference, file.getBytes());
        return ResponseEntity.status(HttpStatus.CREATED).body(BatchImportResponse.from(result));
    }
}
