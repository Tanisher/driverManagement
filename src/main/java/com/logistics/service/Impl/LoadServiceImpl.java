package com.logistics.service.Impl;

import com.logistics.DTO.LoadBillableAmountResponse;
import com.logistics.entity.Customer;
import com.logistics.entity.Driver;
import com.logistics.entity.Load;
import com.logistics.entity.LoadDTO;
import com.logistics.entity.LoadedTrip;
import com.logistics.entity.PricingMode;
import com.logistics.entity.TripStatus;
import com.logistics.entity.Vehicle;
import com.logistics.repository.CustomerRepository;
import com.logistics.repository.DriverRepository;
import com.logistics.repository.LoadRepository;
import com.logistics.repository.LoadedTripRepository;
import com.logistics.repository.VehicleRepository;
import com.logistics.service.CargoCompatibility;
import com.logistics.service.LoadPricing;
import com.logistics.service.LoadService;
import com.logistics.util.DriverAssignmentRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class LoadServiceImpl implements LoadService {
    private final LoadRepository loadRepository;
    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final LoadedTripRepository loadedTripRepository;

    public LoadServiceImpl(LoadRepository loadRepository,
                           CustomerRepository customerRepository,
                           DriverRepository driverRepository,
                           VehicleRepository vehicleRepository,
                           LoadedTripRepository loadedTripRepository) {
        this.loadRepository = loadRepository;
        this.customerRepository = customerRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.loadedTripRepository = loadedTripRepository;
    }


    @Override
    public Load saveLoad(Load load) {
        if (load.getCustomerId() == null) {
            throw new RuntimeException("Customer ID is required");
        }

        Customer customer = customerRepository.findById(load.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        load.setCustomer(customer);
        if (load.getStatus() == null || load.getStatus().isBlank()) {
            load.setStatus(Load.STATUS_PENDING);
        }
        return persistValidated(load);
    }

    @Override
    public Load updateLoad(Long id, Load incoming) {
        Load existing = requireLoad(id);
        copyLoadFields(incoming, existing, false);
        return persistValidated(existing);
    }

    @Override
    public Load patchLoad(Long id, LoadDTO patch) {
        Load existing = requireLoad(id);
        applyLoadPatch(patch, existing);
        return persistValidated(existing);
    }

    @Override
    public List<Load> getAllLoads() {
        return loadRepository.findAll();
    }

    @Override
    public Load getLoadById(Long id) {
        return requireLoad(id);
    }

    @Override
    public void deleteLoad(Long id) {
        loadRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Load assignDriver(Long loadId, DriverAssignmentRequest request) {
        if (request == null || request.getDriverId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "driverId is required");
        }

        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Load not found"));

        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));

        Vehicle vehicle = vehicleRepository.findByDriver(driver)
                .or(() -> Optional.ofNullable(driver.getVehicle()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Driver has no standing vehicle assignment"));

        CargoCompatibility.assertVehicleCanCarry(vehicle.getVehicleType(), load.getCargoType());

        load.setAssignedDriver(driver);
        return loadRepository.save(load);
    }

    @Override
    public LoadBillableAmountResponse getBillableAmount(Long loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Load not found"));

        LoadedTrip loadedTrip = loadedTripRepository
                .findFirstByLoadIdAndStatusOrderByIdDesc(loadId, TripStatus.COMPLETED)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Loaded trip is not complete"));

        return LoadBillableAmountResponse.builder()
                .loadId(load.getId())
                .pricingMode(load.getPricingMode())
                .billableAmount(LoadPricing.billableAmount(
                        load.getPricingMode(), load.getRatePerKm(), load.getFlatAmount(), loadedTrip))
                .loadedKm(load.getPricingMode() == PricingMode.PER_KM
                        ? LoadPricing.loadedDistanceKm(loadedTrip)
                        : null)
                .build();
    }

    @Override
    public LoadDTO convertToDTO(Load load) {
        LoadDTO dto = new LoadDTO();
        dto.setId(load.getId());
        dto.setCustomerId(load.getCustomerId());
        dto.setDescription(load.getDescription());
        dto.setStatus(load.getStatus());
        dto.setCargoType(load.getCargoType());
        dto.setPricingMode(load.getPricingMode());
        dto.setRatePerKm(load.getRatePerKm());
        dto.setFlatAmount(load.getFlatAmount());
        dto.setDeliveryLocation(load.getDeliveryLocation());
        dto.setPickupLocation(load.getPickupLocation());
        dto.setWeight(load.getWeight());

        if (load.getAssignedDriver() != null) {
            dto.setAssignedDriverId(load.getAssignedDriver().getId());
        }

        if (load.getCustomer() != null) {
            dto.setCustomerName(load.getCustomer().getName());
        }

        return dto;

    }

    private Load persistValidated(Load load) {
        if (load.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer ID is required");
        }
        Customer customer = customerRepository.findById(load.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        load.setCustomer(customer);
        LoadPricing.validate(load.getPricingMode(), load.getRatePerKm(), load.getFlatAmount());
        return loadRepository.save(load);
    }

    private void copyLoadFields(Load incoming, Load existing, boolean partial) {
        if (!partial || incoming.getCustomerId() != null) {
            existing.setCustomerId(incoming.getCustomerId());
        }
        if (!partial || incoming.getDescription() != null) {
            existing.setDescription(incoming.getDescription());
        }
        if (!partial || incoming.getWeight() != null) {
            existing.setWeight(incoming.getWeight());
        }
        if (!partial || incoming.getPickupLocation() != null) {
            existing.setPickupLocation(incoming.getPickupLocation());
        }
        if (!partial || incoming.getDeliveryLocation() != null) {
            existing.setDeliveryLocation(incoming.getDeliveryLocation());
        }
        if (!partial || incoming.getStatus() != null) {
            existing.setStatus(incoming.getStatus());
        }
        if (!partial || incoming.getCargoType() != null) {
            existing.setCargoType(incoming.getCargoType());
        }
        if (!partial || incoming.getPricingMode() != null) {
            existing.setPricingMode(incoming.getPricingMode());
        }
        if (!partial || incoming.getRatePerKm() != null) {
            existing.setRatePerKm(incoming.getRatePerKm());
        }
        if (!partial || incoming.getFlatAmount() != null) {
            existing.setFlatAmount(incoming.getFlatAmount());
        }
        if (!partial) {
            if (existing.getPricingMode() == PricingMode.PER_KM) {
                existing.setFlatAmount(null);
            } else if (existing.getPricingMode() == PricingMode.FLAT) {
                existing.setRatePerKm(null);
            }
        }
    }

    private void applyLoadPatch(LoadDTO patch, Load existing) {
        if (patch.getCustomerId() != null) {
            existing.setCustomerId(patch.getCustomerId());
        }
        if (patch.getDescription() != null) {
            existing.setDescription(patch.getDescription());
        }
        if (patch.getWeight() != null) {
            existing.setWeight(patch.getWeight());
        }
        if (patch.getPickupLocation() != null) {
            existing.setPickupLocation(patch.getPickupLocation());
        }
        if (patch.getDeliveryLocation() != null) {
            existing.setDeliveryLocation(patch.getDeliveryLocation());
        }
        if (patch.getStatus() != null) {
            existing.setStatus(patch.getStatus());
        }
        if (patch.getCargoType() != null) {
            existing.setCargoType(patch.getCargoType());
        }
        if (patch.getPricingMode() != null) {
            existing.setPricingMode(patch.getPricingMode());
            if (patch.getPricingMode() == PricingMode.PER_KM && patch.getFlatAmount() == null) {
                existing.setFlatAmount(null);
            }
            if (patch.getPricingMode() == PricingMode.FLAT && patch.getRatePerKm() == null) {
                existing.setRatePerKm(null);
            }
        }
        if (patch.getRatePerKm() != null) {
            existing.setRatePerKm(patch.getRatePerKm());
        }
        if (patch.getFlatAmount() != null) {
            existing.setFlatAmount(patch.getFlatAmount());
        }
    }

    private Load requireLoad(Long id) {
        return loadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Load not found"));
    }
}
