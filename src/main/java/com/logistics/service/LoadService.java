package com.logistics.service;

import com.logistics.DTO.LoadBillableAmountResponse;
import com.logistics.entity.Load;
import com.logistics.entity.LoadDTO;
import com.logistics.util.DriverAssignmentRequest;

import java.util.List;

public interface LoadService {
    Load saveLoad(Load load);
    Load updateLoad(Long id, Load load);
    Load patchLoad(Long id, LoadDTO patch);
    List<Load> getAllLoads();
    Load getLoadById(Long id);
    void deleteLoad(Long id);
    Load assignDriver(Long loadId, DriverAssignmentRequest request);
    LoadBillableAmountResponse getBillableAmount(Long loadId);
    LoadDTO convertToDTO(Load savedLoad);
}
