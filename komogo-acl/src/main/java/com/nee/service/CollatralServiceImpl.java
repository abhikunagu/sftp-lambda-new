package com.nee.service;

import com.nee.model.GuaranteeInstrumentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CollatralServiceImpl implements CollatralService {

    @Override
    public ResponseEntity<?> pushEvent() {
        GuaranteeInstrumentDTO dto = GuaranteeInstrumentDTO.builder()
                .amdSystemId("123456")
                .actualAmount(BigDecimal.valueOf(1000.00))
                .build();
        return ResponseEntity.ok(dto);
    }
}
