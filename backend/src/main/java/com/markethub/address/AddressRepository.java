package com.markethub.address;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserIdOrderByDefaultAddressDescCreatedAtDesc(Long userId);
    List<Address> findByUserIdOrderByCreatedAtAsc(Long userId);
    long countByUserId(Long userId);
}
