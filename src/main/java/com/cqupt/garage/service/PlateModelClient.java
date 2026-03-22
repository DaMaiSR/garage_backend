package com.cqupt.garage.service;

import com.cqupt.garage.dto.PlateModelResultDTO;

public interface PlateModelClient {

    PlateModelResultDTO recognize(byte[] imageBytes, String originalFilename);
}
