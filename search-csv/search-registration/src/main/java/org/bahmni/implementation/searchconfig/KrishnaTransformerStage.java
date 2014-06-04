package org.bahmni.implementation.searchconfig;

import org.bahmni.csv.FailedRowResult;
import org.bahmni.csv.SimpleStage;
import org.bahmni.csv.StageResult;
import org.bahmni.csv.exception.MigrationException;
import org.bahmni.fonttransform.KrishnaFontTransformer;

import java.util.ArrayList;
import java.util.List;

public class KrishnaTransformerStage implements SimpleStage<SearchCSVRow> {
    @Override
    public String getName() {
        return "KRISHNA_FONT_TRANSFORM";
    }

    @Override
    public boolean canRunInParallel() {
        return false;
    }

    @Override
    public StageResult execute(List<SearchCSVRow> csvEntityList) throws MigrationException {
        KrishnaFontTransformer transformer = new KrishnaFontTransformer();
        List<FailedRowResult<SearchCSVRow>> failedRowResults = new ArrayList<FailedRowResult<SearchCSVRow>>();
        for (SearchCSVRow csvRow : csvEntityList) {
            try{
                csvRow.prefix = transformer.krishnaToUnicode(csvRow.prefix);
                csvRow.firstName = transformer.krishnaToUnicode(csvRow.firstName);
                csvRow.middleName = transformer.krishnaToUnicode(csvRow.middleName);
                csvRow.lastName = transformer.krishnaToUnicode(csvRow.lastName);
            }catch (Exception e){
                failedRowResults.add(new FailedRowResult<SearchCSVRow>(csvRow, e.getMessage()));
            }
        }
        return new StageResult(getName(), failedRowResults, csvEntityList);
    }
}
