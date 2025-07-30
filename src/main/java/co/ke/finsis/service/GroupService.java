// co.ke.finsis.service.GroupService
package co.ke.finsis.service;

import co.ke.finsis.entity.ClientInfo;
import co.ke.finsis.entity.Group;
import co.ke.finsis.entity.OfficerRegistration;
import co.ke.finsis.payload.ClientDto; // Import ClientDto
import co.ke.finsis.payload.GroupDTO;
import co.ke.finsis.repository.GroupRepository;
import co.ke.finsis.repository.OfficerRegistrationRepository;
import co.ke.tucode.accounting.entities.Account;
import co.ke.tucode.accounting.repositories.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final OfficerRegistrationRepository officerRepo;
    private final AccountRepository accountRepository;
    private final ClientInfoService clientInfoService; // Inject ClientInfoService

    public GroupDTO createGroup(GroupDTO dto) {
        Group group = mapToEntity(dto);
        // Clients are typically added to a group via the ClientInfoService when creating/updating a client,
        // so we don't necessarily process a list of clients in createGroup here.
        // The clients list in GroupDTO might primarily be for displaying existing clients.
        return mapToDTO(groupRepository.save(group));
    }

    public GroupDTO getGroupById(Long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));
        return mapToDTO(group);
    }

    public List<GroupDTO> getAllGroups() {
        return groupRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public GroupDTO updateGroup(Long id, GroupDTO dto) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found"));

        group.setGroupName(dto.getGroupName());
        group.setCounty(dto.getCounty());
        group.setSubCounty(dto.getSubCounty());
        group.setWard(dto.getWard());
        group.setVillage(dto.getVillage());
        group.setNearestLandmark(dto.getNearestLandmark());
        group.setOfficeType(dto.getOfficeType());

        if (dto.getOfficerId() != null) {
            OfficerRegistration officer = officerRepo.findById(dto.getOfficerId())
                    .orElseThrow(() -> new EntityNotFoundException("Officer not found"));
            group.setOfficer(officer);
        }

        // You might want to handle client updates here if the DTO allows modifying clients directly in a group.
        // However, it's more common to manage client-group relationships from the ClientInfoService side
        // when a client is created or updated.

        return mapToDTO(groupRepository.save(group));
    }

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    private GroupDTO mapToDTO(Group group) {
        // You already have group.getClients() which should be populated if properly configured
        // in your Group entity (e.g., @OneToMany with ClientInfo).
        // Convert the list of ClientInfo entities to ClientDto.
        List<ClientDto> clientDtos = group.getClients() != null
                ? group.getClients().stream()
                .map(clientInfoService::mapToClientDto) // Use clientInfoService to map ClientInfo to ClientDto
                .collect(Collectors.toList())
                : new ArrayList<>();

        return GroupDTO.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .county(group.getCounty())
                .subCounty(group.getSubCounty())
                .ward(group.getWard())
                .village(group.getVillage())
                .nearestLandmark(group.getNearestLandmark())
                .officeType(group.getOfficeType())
                .officerId(group.getOfficer() != null ? group.getOfficer().getId() : null)
                .officerName(group.getOfficer() != null ? group.getOfficer().getFullName() : null)
                .savingbalance(calculateGroupSavingsBalance(group))
                .totalClients(group.getClients() != null ? group.getClients().size() : 0)
                .clients(clientDtos) // Set the list of client DTOs
                .build();
    }

    private Group mapToEntity(GroupDTO dto) {
        Group group = Group.builder()
                .groupName(dto.getGroupName())
                .county(dto.getCounty())
                .subCounty(dto.getSubCounty())
                .ward(dto.getWard())
                .village(dto.getVillage())
                .nearestLandmark(dto.getNearestLandmark())
                .officeType(dto.getOfficeType())
                .clients(new ArrayList<>()) // Initialize clients list (will be populated by ClientInfoService)
                .build();

        if (dto.getOfficerId() != null) {
            OfficerRegistration officer = officerRepo.findById(dto.getOfficerId())
                    .orElseThrow(() -> new EntityNotFoundException("Officer not found"));
            group.setOfficer(officer);
        }

        return group;
    }

    public Optional<GroupDTO> getGroupByName(String groupName) {
        return groupRepository.findByGroupName(groupName)
                .map(this::mapToDTO);
    }

    public Optional<GroupDTO> updateGroupByName(String groupName, GroupDTO dto) {
        return groupRepository.findByGroupName(groupName)
                .map(group -> {
                    group.setCounty(dto.getCounty());
                    group.setSubCounty(dto.getSubCounty());
                    group.setWard(dto.getWard());
                    group.setVillage(dto.getVillage());
                    group.setNearestLandmark(dto.getNearestLandmark());
                    group.setOfficeType(dto.getOfficeType());

                    if (dto.getOfficerId() != null) {
                        OfficerRegistration officer = officerRepo.findById(dto.getOfficerId())
                                .orElseThrow(() -> new EntityNotFoundException("Officer not found"));
                        group.setOfficer(officer);
                    }

                    return mapToDTO(groupRepository.save(group));
                });
    }

    public boolean deleteGroupByName(String groupName) {
        return groupRepository.findByGroupName(groupName)
                .map(group -> {
                    groupRepository.delete(group);
                    return true;
                }).orElse(false);
    }

    public List<GroupDTO> updateGroupsBatch(List<GroupDTO> dtos) {
        List<GroupDTO> updatedGroups = new ArrayList<>();
        for (GroupDTO dto : dtos) {
            GroupDTO updatedGroup = updateGroup(dto.getId(), dto);
            updatedGroups.add(updatedGroup);
        }
        return updatedGroups;
    }

    public BigDecimal calculateGroupSavingsBalance(Group group) {
        List<ClientInfo> clients = group.getClients();
        List<Long> accountIds = clients.stream()
                .flatMap(client -> client.getAccounts().stream())
                .map(Account::getId)
                .toList();

        if (accountIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<Account> accounts = accountRepository.findAllById(accountIds);

        return accounts.stream()
                .map(Account::getBalance)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}