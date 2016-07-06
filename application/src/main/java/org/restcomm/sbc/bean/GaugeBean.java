package org.restcomm.sbc.bean;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.chart.MeterGaugeChartModel;

public class GaugeBean implements Serializable {

 private MeterGaugeChartModel meterGaugeModel;

 public MeterGaugeChartModel getMeterGaugeModel() {
  meterGaugeModel.setValue(Math.random() * 220);
  return meterGaugeModel;
 }
 public GaugeBean() {
  createMeterGaugeModel();
 }
 private void createMeterGaugeModel() {
  List<Number> intervals = new ArrayList<Number>() {
   {
    add(20);
    add(50);
    add(120);
    add(220);
   }
  };
  meterGaugeModel = new MeterGaugeChartModel(0, intervals);
  meterGaugeModel.setTitle("Custom Options");
  meterGaugeModel.setSeriesColors("66cc66,93b75f,E7E658,cc6666");
  meterGaugeModel.setGaugeLabel("km/h");
  meterGaugeModel.setGaugeLabelPosition("bottom");
  meterGaugeModel.setShowTickLabels(false);
  meterGaugeModel.setLabelHeightAdjust(110);
  meterGaugeModel.setIntervalOuterRadius(130);
 }

}

