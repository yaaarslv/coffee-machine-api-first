package org.isrpo.repositories;

import org.isrpo.entities.MachineInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Database repository for MachineInventory entity
 */
@Repository
public interface MachineInventoryRepository extends JpaRepository<MachineInventory, Long> {
}
