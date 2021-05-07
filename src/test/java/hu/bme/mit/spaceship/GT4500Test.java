package hu.bme.mit.spaceship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class GT4500Test {

  private TorpedoStore mockPrimaryTS;
  private TorpedoStore mockSecondaryTS;
  private GT4500 ship;

  @BeforeEach
  public void init(){
    mockPrimaryTS = mock(TorpedoStore.class);
    mockSecondaryTS = mock(TorpedoStore.class);
    ship = new GT4500(mockPrimaryTS, mockSecondaryTS);
  }

  @Test
  public void fireTorpedo_Single_Success(){
    // Arrange
    when(mockPrimaryTS.fire(1)).thenReturn(true);

    // Act
    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    // Assert
    assertEquals(true, result);
    verify(mockPrimaryTS, times(1)).fire(1);
  }

  @Test
  public void fireTorpedo_All_Success(){
    // Arrange
    when(mockPrimaryTS.fire(1)).thenReturn(true);
    when(mockSecondaryTS.fire(1)).thenReturn(true);

    // Act
    boolean result = ship.fireTorpedo(FiringMode.ALL);

    // Assert
    assertEquals(true, result);
    verify(mockPrimaryTS, times(1)).fire(1);
    verify(mockSecondaryTS, times(1)).fire(1);
  }

  @Test
  public void fireTorpedo_Single_repeatedCallsFireAlternating() {
    when(mockPrimaryTS.fire(1)).thenReturn(true);
    when(mockSecondaryTS.fire(1)).thenReturn(true);

    boolean result1 = ship.fireTorpedo(FiringMode.SINGLE);
    boolean result2 = ship.fireTorpedo(FiringMode.SINGLE);

    assertEquals(true, result1);
    assertEquals(true, result2);
    verify(mockPrimaryTS, times(1)).fire(1);
    verify(mockSecondaryTS, times(1)).fire(1);
  }

  @Test
  public void fireTorpedo_Single_ifEmptyTheOtherIsFired_primary() {
    when(mockPrimaryTS.isEmpty()).thenReturn(true);
    when(mockSecondaryTS.fire(1)).thenReturn(true);

    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    assertEquals(true, result);
    verify(mockPrimaryTS, times(0)).fire(anyInt());
    verify(mockSecondaryTS, times(1)).fire(1);
  }

  @Test
  public void fireTorpedo_Single_ifEmptyTheOtherIsFired_secondary() {
    when(mockSecondaryTS.isEmpty()).thenReturn(true);
    when(mockPrimaryTS.fire(1)).thenReturn(true);
    ship.fireTorpedo(FiringMode.SINGLE); // Preliminary call: we want to test when the secondaryStore is the one being fired
    
    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    assertEquals(true, result);
    verify(mockSecondaryTS, times(0)).fire(anyInt());
    verify(mockPrimaryTS, times(2)).fire(1);
  }

  @Test
  public void fireTorpedo_Single_ifFailTheOtherIsNotFired_primary() {
    when(mockPrimaryTS.fire(1)).thenReturn(false);
    
    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    assertEquals(false, result);
    verify(mockPrimaryTS, times(1)).fire(1);
    verify(mockSecondaryTS, times(0)).fire(anyInt());
  }

  @Test
  public void fireTorpedo_Single_ifFailTheOtherIsNotFired_secondary() {
    when(mockPrimaryTS.fire(1)).thenReturn(true);
    ship.fireTorpedo(FiringMode.SINGLE); // Preliminary call: we want to test when the secondaryStore is the one being fired
    when(mockSecondaryTS.fire(1)).thenReturn(false);
    
    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    assertEquals(false, result);
    verify(mockSecondaryTS, times(1)).fire(1);
    verify(mockPrimaryTS, times(1)).fire(1); // Because it's fired in the preliminary call (but never again!)
  }

  @Test
  public void fireTorpedo_All_ifOneFailsReturnsTrue_primary() {
    when(mockPrimaryTS.fire(1)).thenReturn(true);
    when(mockSecondaryTS.fire(1)).thenReturn(false);
    
    boolean result = ship.fireTorpedo(FiringMode.ALL);

    assertEquals(true, result);
    verify(mockPrimaryTS, times(1)).fire(1);
    verify(mockSecondaryTS, times(1)).fire(1);
  }

  @Test
  public void fireTorpedo_All_ifOneFailsReturnsTrue_secondary() {
    when(mockPrimaryTS.fire(1)).thenReturn(false);
    when(mockSecondaryTS.fire(1)).thenReturn(true);
    
    boolean result = ship.fireTorpedo(FiringMode.ALL);

    assertEquals(true, result);
    verify(mockPrimaryTS, times(1)).fire(1);
    verify(mockSecondaryTS, times(1)).fire(1);
  }

  @Test
  public void fireTorpedo_All_ifBothFailReturnsFalse() {
    when(mockPrimaryTS.fire(1)).thenReturn(false);
    when(mockSecondaryTS.fire(1)).thenReturn(false);
    
    boolean result = ship.fireTorpedo(FiringMode.ALL);

    assertEquals(false, result);
    verify(mockPrimaryTS, times(1)).fire(1);
    verify(mockSecondaryTS, times(1)).fire(1);
  }

  @Test
  public void fireTorpedo_All_onlyFireIfNotEmptyForBoth() {
    when(mockPrimaryTS.isEmpty()).thenReturn(true);
    when(mockSecondaryTS.isEmpty()).thenReturn(true);

    boolean result = ship.fireTorpedo(FiringMode.ALL);

    assertEquals(false, result);
    verify(mockPrimaryTS, times(0)).fire(anyInt());
    verify(mockSecondaryTS, times(0)).fire(anyInt());
  }

  @Test
  public void fireTorpedo_Single_noneFiredIfBothEmpty_primary() {
    when(mockPrimaryTS.isEmpty()).thenReturn(true);
    when(mockSecondaryTS.isEmpty()).thenReturn(true);

    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    assertEquals(false, result);
    verify(mockPrimaryTS, times(0)).fire(anyInt());
    verify(mockSecondaryTS, times(0)).fire(anyInt());
  }

  @Test
  public void fireTorpedo_Single_noneFiredIfBothEmpty_secondary() {
    when(mockPrimaryTS.fire(1)).thenReturn(true);
    ship.fireTorpedo(FiringMode.SINGLE); // We have to fire the primary properly once
    when(mockPrimaryTS.isEmpty()).thenReturn(true);
    when(mockSecondaryTS.isEmpty()).thenReturn(true);

    boolean result = ship.fireTorpedo(FiringMode.SINGLE);

    assertEquals(false, result);
    verify(mockPrimaryTS, times(1)).fire(anyInt()); // In the arranging phase
    verify(mockSecondaryTS, times(0)).fire(anyInt());
  }
}
