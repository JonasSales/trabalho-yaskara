package com.br.projetoyaskara.service;

import com.br.projetoyaskara.dto.EnderecoDTO;
import com.br.projetoyaskara.exception.ResourceNotFoundException;
import com.br.projetoyaskara.mapper.EnderecoMapper;
import com.br.projetoyaskara.model.Endereco;
import com.br.projetoyaskara.model.Eventos;
import com.br.projetoyaskara.model.Organizacao;
import com.br.projetoyaskara.model.clientuser.ClientUser;
import com.br.projetoyaskara.repository.EnderecoRepository;
import com.br.projetoyaskara.repository.EventosRepository;
import com.br.projetoyaskara.repository.OrganizacaoRepository;
import com.br.projetoyaskara.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.br.projetoyaskara.util.Utils.atualizarEndereco;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UserRepository userRepository;
    private final EnderecoMapper enderecoMapper;
    private final EventosRepository eventosRepository;
    private final OrganizacaoRepository organizacaoRepository;

    public EnderecoService(EnderecoRepository enderecoRepository, UserRepository userRepository,
                           EnderecoMapper enderecoMapper, EventosRepository eventosRepository,
                           OrganizacaoRepository organizacaoRepository) {
        this.enderecoRepository = enderecoRepository;
        this.userRepository = userRepository;
        this.enderecoMapper = enderecoMapper;
        this.eventosRepository = eventosRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    private Endereco findEnderecoOrThrow(long id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereco não encontrado"));
    }

    private Eventos findEventosOrThrow(long id) {
        return eventosRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado"));
    }

    private Organizacao findOrganizacaoOrThrow(UUID id) {
        return organizacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada"));
    }

    // ===================== CLIENTE =====================

    public ResponseEntity<EnderecoDTO> cadastrarEnderecoClient(Authentication authentication, EnderecoDTO enderecoDTO) {
        ClientUser clientUser = userRepository.findByEmail(authentication.getName());

        if (clientUser.getEndereco() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        Endereco endereco = enderecoMapper.toEntity(enderecoDTO);
        clientUser.setEndereco(endereco);
        ClientUser atualizado = userRepository.save(clientUser);

        return ResponseEntity.ok(enderecoMapper.toDto(atualizado.getEndereco()));
    }

    public ResponseEntity<EnderecoDTO> atualizarEnderecoClient(Authentication authentication, EnderecoDTO enderecoDTO) {
        ClientUser clientUser = userRepository.findByEmail(authentication.getName());

        if (clientUser.getEndereco() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Endereco enderecoDesatualizado = findEnderecoOrThrow(clientUser.getEndereco().getId());
        Endereco enderecoAtualizado = enderecoMapper.toEntity(enderecoDTO);
        atualizarEndereco(enderecoDesatualizado, enderecoAtualizado);
        enderecoRepository.save(enderecoDesatualizado);
        return ResponseEntity.ok(enderecoMapper.toDto(enderecoDesatualizado));
    }

    public ResponseEntity<EnderecoDTO> buscarEnderecoCLient(Authentication authentication) {
        ClientUser clientUser = userRepository.findByEmail(authentication.getName());

        if (clientUser.getEndereco() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(enderecoMapper.toDto(findEnderecoOrThrow(clientUser.getEndereco().getId())));
    }

    public ResponseEntity<String> deletarEnderecoClient(Authentication authentication) {
        ClientUser clientUser = userRepository.findByEmail(authentication.getName());

        if (clientUser.getEndereco() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não possui endereço.");
        }

        Endereco endereco = findEnderecoOrThrow(clientUser.getEndereco().getId());
        clientUser.setEndereco(null);
        userRepository.save(clientUser);
        enderecoRepository.delete(endereco);
        return ResponseEntity.ok("Endereço deletado");
    }

    // ===================== EVENTO =====================

    public ResponseEntity<EnderecoDTO> cadastrarEnderecoEvento(Authentication authentication, EnderecoDTO enderecoDTO, long idEvento) {
        UUID clientId = userRepository.findIdByEmail(authentication.getName());
        UUID organizacaoId = organizacaoRepository.findOrganizacaoByEventosId(idEvento);

        if (!clientId.equals(organizacaoId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Eventos evento = findEventosOrThrow(idEvento);

        if (evento.getEndereco() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Endereco endereco = enderecoMapper.toEntity(enderecoDTO);
        evento.setEndereco(endereco);
        eventosRepository.save(evento);

        return ResponseEntity.ok(enderecoMapper.toDto(endereco));
    }

    public ResponseEntity<EnderecoDTO> atualizarEnderecoEvento(Authentication authentication, long idEvento, EnderecoDTO enderecoDTO) {
        UUID clientId = userRepository.findIdByEmail(authentication.getName());
        Eventos evento = findEventosOrThrow(idEvento);
        UUID organizacaoId = organizacaoRepository.findOrganizacaoByEventosId(idEvento);

        if (!organizacaoId.equals(clientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (evento.getEndereco() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Endereco enderecoDesatualizado = findEnderecoOrThrow(evento.getEndereco().getId());
        Endereco enderecoAtualizado = enderecoMapper.toEntity(enderecoDTO);
        atualizarEndereco(enderecoDesatualizado, enderecoAtualizado);
        enderecoRepository.save(enderecoDesatualizado);

        return ResponseEntity.ok(enderecoMapper.toDto(enderecoDesatualizado));
    }

    public ResponseEntity<String> deletarEnderecoEvento(Authentication authentication, long idEvento) {
        UUID clientId = userRepository.findIdByEmail(authentication.getName());
        UUID organizacaoId = organizacaoRepository.findOrganizacaoByEventosId(idEvento);

        if (!clientId.equals(organizacaoId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
        }

        Eventos evento = findEventosOrThrow(idEvento);
        Endereco endereco = evento.getEndereco();

        if (endereco == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("O evento não possui endereço.");
        }

        evento.setEndereco(null);
        eventosRepository.save(evento);
        enderecoRepository.delete(endereco);

        return ResponseEntity.ok("Endereço do evento deletado");
    }

    public ResponseEntity<EnderecoDTO> buscarEnderecoEvento(Authentication authentication, long idEvento) {
        UUID clientId = userRepository.findIdByEmail(authentication.getName());
        UUID organizacaoId = organizacaoRepository.findOrganizacaoByEventosId(idEvento);

        if (!clientId.equals(organizacaoId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Eventos evento = findEventosOrThrow(idEvento);

        if (evento.getEndereco() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(enderecoMapper.toDto(evento.getEndereco()));
    }

    // ===================== ORGANIZAÇÃO =====================

    public ResponseEntity<EnderecoDTO> cadastrarEnderecoOrganizacao(Authentication authentication, UUID idOrganizacao, EnderecoDTO enderecoDTO) {
        UUID clientId = userRepository.findIdByEmail(authentication.getName());
        Organizacao organizacao = findOrganizacaoOrThrow(idOrganizacao);

        if (!clientId.equals(organizacao.getProprietario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (organizacao.getEndereco() != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Endereco endereco = enderecoMapper.toEntity(enderecoDTO);
        enderecoRepository.save(endereco);
        organizacao.setEndereco(endereco);
        organizacaoRepository.save(organizacao);

        return ResponseEntity.ok(enderecoMapper.toDto(endereco));
    }

    public ResponseEntity<EnderecoDTO> atualizarEnderecoOrganizacao(Authentication authentication, UUID idOrganizacao, EnderecoDTO enderecoDTO) {
        UUID clientId = userRepository.findIdByEmail(authentication.getName());
        Organizacao organizacao = findOrganizacaoOrThrow(idOrganizacao);

        if (!clientId.equals(organizacao.getProprietario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (organizacao.getEndereco() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Endereco enderecoDesatualizado = findEnderecoOrThrow(organizacao.getEndereco().getId());
        Endereco enderecoAtualizado = enderecoMapper.toEntity(enderecoDTO);
        atualizarEndereco(enderecoDesatualizado, enderecoAtualizado);
        enderecoRepository.save(enderecoDesatualizado);

        return ResponseEntity.ok(enderecoMapper.toDto(enderecoDesatualizado));
    }

    public ResponseEntity<String> deletarEnderecoOrganizacao(Authentication authentication, UUID idOrganizacao) {
        UUID clientId = userRepository.findIdByEmail(authentication.getName());
        Organizacao organizacao = findOrganizacaoOrThrow(idOrganizacao);

        if (!clientId.equals(organizacao.getProprietario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: você não é o proprietário da organização.");
        }

        Endereco endereco = organizacao.getEndereco();
        if (endereco == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("A organização não possui endereço.");
        }

        organizacao.setEndereco(null);
        organizacaoRepository.save(organizacao);
        enderecoRepository.delete(endereco);

        return ResponseEntity.ok("Endereço da organização deletado com sucesso.");
    }

    public ResponseEntity<EnderecoDTO> buscarEnderecoOrganizacao(Authentication authentication, UUID idOrganizacao) {
        UUID clientId = userRepository.findIdByEmail(authentication.getName());
        Organizacao organizacao = findOrganizacaoOrThrow(idOrganizacao);

        if (!clientId.equals(organizacao.getProprietario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (organizacao.getEndereco() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(enderecoMapper.toDto(organizacao.getEndereco()));
    }
}
