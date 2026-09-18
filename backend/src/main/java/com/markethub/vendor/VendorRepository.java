package com.markethub.vendor;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface VendorRepository extends JpaRepository<Vendor,Long>{Optional<Vendor> findByUserId(Long userId);boolean existsByUserId(Long userId);List<Vendor> findByStatus(VendorStatus status);long countByStatus(VendorStatus status);}