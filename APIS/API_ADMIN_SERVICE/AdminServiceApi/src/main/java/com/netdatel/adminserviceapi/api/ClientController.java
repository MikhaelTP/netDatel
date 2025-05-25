package com.netdatel.adminserviceapi.api;

import com.netdatel.adminserviceapi.security.CurrentUserId;  // ← Import correcto
import com.netdatel.adminserviceapi.dto.request.ClientRequest;
import com.netdatel.adminserviceapi.dto.response.ClientHistoryResponse;
import com.netdatel.adminserviceapi.dto.response.ClientResponse;
import com.netdatel.adminserviceapi.entity.enums.ClientStatus;
import com.netdatel.adminserviceapi.service.ClientHistoryService;
import com.netdatel.adminserviceapi.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final ClientHistoryService clientHistoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('admin:client:create')")
    public ClientResponse createClient(
            @RequestBody @Valid ClientRequest request,
            @CurrentUserId Integer userId) {
        return clientService.createClient(request, userId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:client:read')")
    public ClientResponse getClient(@PathVariable Integer id) {
        return clientService.getClientById(id);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('admin:client:read')")
    public ClientResponse getClientByCode(@PathVariable String code) {
        return clientService.getClientByCode(code);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:client:read')")
    public ResponseEntity<Page<ClientResponse>> getAllClients(Pageable pageable) {
        return ResponseEntity.ok(clientService.getAllClients(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('admin:client:read')")
    public List<ClientResponse> searchClients(@RequestParam String term) {
        return clientService.searchClients(term);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:client:update')")
    public ClientResponse updateClient(
            @PathVariable Integer id,
            @RequestBody @Valid ClientRequest request,
            @CurrentUserId Integer userId) {
        return clientService.updateClient(id, request, userId);
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('admin:client:update')")
    public void changeClientStatus(
            @PathVariable Integer id,
            @RequestParam ClientStatus status,
            @CurrentUserId Integer userId) {
        clientService.changeClientStatus(id, status, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('admin:client:delete')")
    public void deleteClient(
            @PathVariable Integer id,
            @CurrentUserId Integer userId) {
        clientService.deleteClient(id, userId);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('admin:client:read')")
    public List<ClientHistoryResponse> getClientHistory(@PathVariable Integer id) {
        return clientHistoryService.getClientHistory(id);
    }
}