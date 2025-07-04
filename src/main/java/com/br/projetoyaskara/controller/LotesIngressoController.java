package com.br.projetoyaskara.controller;

import com.br.projetoyaskara.dto.FaixaDePreco;
import com.br.projetoyaskara.dto.LotesIngressoDTO;
import com.br.projetoyaskara.service.LotesIngressosService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingressos")
public class LotesIngressoController {

    final private LotesIngressosService lotesIngressosService;

    public LotesIngressoController(LotesIngressosService lotesIngressosService) {
        this.lotesIngressosService = lotesIngressosService;
    }

    @PreAuthorize("hasAnyAuthority('organization::create')")
    @PostMapping()
    public ResponseEntity<LotesIngressoDTO> create(Authentication authentication,
                                                   @Valid @RequestBody LotesIngressoDTO lotesIngressoDTO) {
        return lotesIngressosService.cadastrarIngresso(authentication, lotesIngressoDTO);
    }

    @PreAuthorize("hasAnyAuthority('organization::put')")
    @PutMapping()
    public ResponseEntity<LotesIngressoDTO> update(Authentication authentication,
                                                   @Valid @RequestBody LotesIngressoDTO lotesIngressoDTO) {
        return lotesIngressosService.atualizarIngresso(authentication, lotesIngressoDTO);
    }

    @PreAuthorize("hasAnyAuthority('organization::delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        return lotesIngressosService.deletarIngresso(authentication, id);
    }

    @GetMapping()
    public ResponseEntity<List<LotesIngressoDTO>> buscarIngressos() {
        return lotesIngressosService.listarIngressos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LotesIngressoDTO> buscarIngressoId(@PathVariable Long id) {
        return lotesIngressosService.buscarIngressoPorId(id);
    }

    @GetMapping("/evento/{id}")
    public ResponseEntity<List<LotesIngressoDTO>> buscarIngressoPorIdEvento(@PathVariable Long id) {
        return lotesIngressosService.buscarIngressosPorEventoId(id);
    }

    @GetMapping("/preco")
    public ResponseEntity<List<LotesIngressoDTO>> buscarIngressoPorPreco(@RequestBody FaixaDePreco faixaDePreco) {
        return lotesIngressosService
                .buscarIngressosPorFaixaDePreco(faixaDePreco.menorPreco(), faixaDePreco.maiorPreco());
    }

}