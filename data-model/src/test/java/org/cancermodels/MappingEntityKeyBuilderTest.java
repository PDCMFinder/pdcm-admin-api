package org.cancermodels;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MappingEntityKeyBuilderTest {

  @Test
  void buildKeyDiagnosisMappingNullValues() {
    String key = MappingEntityKeyBuilder.buildKeyDiagnosisMapping(null, null, null, null);
    String nullValuesDiagnosisSHA256
        = "b6646f0096f23ec519a3283db47a0d11913175638e840bf27acb89cf2c3a1fb3";
    assertEquals(nullValuesDiagnosisSHA256, key);
  }

  @Test
  void buildKeyDiagnosisMappingValues() {
    String key = MappingEntityKeyBuilder.buildKeyDiagnosisMapping(
        "sampleDiagnosisTest", "tumorTypeTest", "originTissueTest", "dataSourceTest");
    // Expected value for text diagnosis|samplediagnosistest|tumortypetest|origintissuetest|datasourcetest
    String testSHA256
        = "7b935ec404bd587ed59fd8eec3ecb82321b5eedbe2f52e813ec148254c5333b2";
    assertEquals(testSHA256, key);
  }

  @Test
  void buildKeyTreatmentMappingNullValues() {
    String key = MappingEntityKeyBuilder.buildKeyTreatmentMapping(null, null);
    // Expected value for text treatment||
    String nullValuesTreatmentSHA256
        = "da74ab8f82c9e044188126042d4f988570b183fb7e02370bcb7ffc1dc1a420ef";
    assertEquals(nullValuesTreatmentSHA256, key);
  }

  @Test
  void buildKeyTreatmentMappingValues() {
    String key = MappingEntityKeyBuilder.buildKeyTreatmentMapping(
        "treatmentNameTest", "dataSourceTest");
    // Expected value for text treatment|treatmentnametest|datasourcetest
    String testSHA256
        = "9811fb52e07de50d4c4afb0caca69d95e367e4a7bb458ca00a99a6823b21e233";
    assertEquals(testSHA256, key);
  }

  @Test
  void buildKeySimilarDiagnosisGivesDifferentKey() {
    String key1 = MappingEntityKeyBuilder.buildKeyDiagnosisMapping(
        "Invasive Ductal Carcinoma, Not Otherwise Specified", "tumorTypeTest",
        "originTissueTest", "dataSourceTest");
    String key2 = MappingEntityKeyBuilder.buildKeyDiagnosisMapping(
        "Invasive Ductal Carcinoma Not Otherwise Specified", "tumorTypeTest",
        "originTissueTest", "dataSourceTest");
    assertNotEquals(key1, key2);
  }
}