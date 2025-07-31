package co.ke.bulkstream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Service
public class OilTonnageService {

    @Autowired
    private OilTonnageRepository oilTonnageRepository;

    @Autowired
    private VcftableRepository vcftableRepository;

    public OilTonnage calculateAndSaveTonnage(Double volume, Double density, Double temperature) {
        // Find VCF
        Vcftable vcfRecord = vcftableRepository.findVcfByClosestDensityAndTemperature(density, temperature)
                .orElseThrow(() -> new ResourceNotFoundException("VCF not found for density: " + density + " and temperature: " + temperature));

        Double vcf = vcfRecord.getVcf();

        // Calculate Tonnage
        Double tonnage = (volume * density * vcf) / 1000;

        // Store result
        OilTonnage oilTonnage = new OilTonnage();
        oilTonnage.setVolume(volume);
        oilTonnage.setDensity(density);
        oilTonnage.setTemperature(temperature);
        oilTonnage.setVcf(vcf);
        oilTonnage.setTonnage(tonnage);
        oilTonnage.setCalculationDate(LocalDateTime.now());

        return oilTonnageRepository.save(oilTonnage);
    }

    public Page<OilTonnage> getAllCalculations(Pageable pageable) {
        return oilTonnageRepository.findAll(pageable);
    }

    public Page<OilTonnage> searchCalculations(String searchTerm, Pageable pageable) {
        return oilTonnageRepository.searchAllFields("%" + searchTerm + "%", pageable);
    }
}