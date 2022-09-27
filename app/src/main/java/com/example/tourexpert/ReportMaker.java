package com.example.tourexpert;

import android.util.Log;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian3d;
import com.anychart.charts.Pie;
import com.anychart.core.cartesian.series.Column3d;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Align;
import com.anychart.enums.HoverMode;
import com.anychart.enums.LegendLayout;
import com.anychart.enums.ScaleStackMode;
import com.anychart.enums.TooltipDisplayMode;
import com.anychart.graphics.vector.SolidFill;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * this class is responsible for generating
 * various statistical reports and setting them
 * to the AnyChartView graph each method has sent as a parameter
 */
public class ReportMaker {

    public static ArrayList<Statistic> data;

    /**
     * generate a yearly graph by months and each category sales per month
     * @param graph - The graph view we will showcase the data
     */
    public static void generate_report_by_months(AnyChartView graph) {
        int[][] data = new int[3][12];

        for (Statistic statistic : ReportMaker.data) {
            int month = Instant.ofEpochMilli(statistic.getDate()).atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue() - 1;

            switch (statistic.getCategory()) {
                case DataBaseCollectionsKeys.FLIGHTS:
                    data[0][month] += statistic.getAmount();
                    break;
                case DataBaseCollectionsKeys.ATTRACTIONS:
                    data[1][month] += statistic.getAmount();
                    break;
                case DataBaseCollectionsKeys.HOTELS:
                    data[2][month] += statistic.getAmount();
                    break;
            }
        }

        ArrayList<DataEntry> seriesData = new ArrayList<DataEntry>();

        for(int c = 0; c < data[0].length; c++) {
            String month = Month.of(c + 1).toString().toLowerCase();
            seriesData.add(
                    new CustomDataEntry(
                        month,
                        data[0][c] != 0 ? data[0][c] : null,
                        data[1][c] != 0 ? data[1][c] : null,
                        data[2][c] != 0 ? data[2][c] : null)
            );
        }

        // create the graph
        Cartesian3d column3d = AnyChart.column3d();

        // region graph settings:
        column3d.yScale().stackMode(ScaleStackMode.VALUE);
        column3d.animation(true);
        column3d.title("Products: ");
        column3d.title().padding(0d, 0d, 15d, 0d);

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Data = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Data = set.mapAs("{ x: 'x', value: 'value1' }");
        Mapping series3Data = set.mapAs("{ x: 'x', value: 'value2' }");

        Column3d series1 = column3d.column(series1Data);
        series1.name("flights");
        series1.fill(new SolidFill("#3e2723", 1d));
        series1.stroke("1 #f7f3f3");
        series1.hovered().stroke("3 #f7f3f3");

        Column3d series2 = column3d.column(series2Data);
        series2.name("attractions");
        series2.fill(new SolidFill("#64b5f6", 1d));
        series2.stroke("1 #f7f3f3");
        series2.hovered().stroke("3 #f7f3f3");

        Column3d series3 = column3d.column(series3Data);
        series3.name("hotels");
        series3.fill(new SolidFill("#fff3e0", 1d));
        series3.stroke("1 #f7f3f3");
        series3.hovered().stroke("3 #f7f3f3");

        column3d.legend().enabled(true);
        column3d.legend().fontSize(13d);
        column3d.legend().padding(0d, 0d, 20d, 0d);

        column3d.xAxis(0).stroke("1 #a18b7e");
        column3d.xAxis(0).labels().fontSize("#a18b7e");
        column3d.yAxis(0).stroke("1 #a18b7e");
        column3d.yAxis(0).labels().fontColor("#a18b7e");
        column3d.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        column3d.yAxis(0).title().enabled(true);
        column3d.yAxis(0).title().text("Sales by months");
        column3d.yAxis(0).title().fontColor("#a18b7e");

        column3d.interactivity().hoverMode(HoverMode.BY_X);

        column3d.tooltip()
                .displayMode(TooltipDisplayMode.UNION)
                .format("{%Value} {%SeriesName}");

        column3d.yGrid(0).stroke("#a18b7e", 1d, null, null, null);
        column3d.xGrid(0).stroke("#a18b7e", 1d, null, null, null);
        // endregion graph settings:

        // set the graph to display:
        graph.setChart(column3d);
    }

    /**
     * generate a yearly pie graph that showcase the total sales for each category
     * compared with the other categories
     * @param graph - The graph view we will showcase the data
     */
    public static void generate_report_by_percentage(AnyChartView graph) {
        int flights_sales_sum = 0;
        int attractions_sales_sum = 0;
        int hotels_sales_sum = 0;

        for(Statistic statistic : ReportMaker.data) {
            switch (statistic.getCategory()) {
                case "flights": flights_sales_sum += statistic.getAmount(); break;
                case "attractions": attractions_sales_sum += statistic.getAmount(); break;
                case "hotels": hotels_sales_sum += statistic.getAmount(); break;
            }
        }

        List<DataEntry> seriesData = new ArrayList<DataEntry>();
        seriesData.add( new ValueDataEntry("flights", flights_sales_sum));
        seriesData.add( new ValueDataEntry("attractions", attractions_sales_sum));
        seriesData.add( new ValueDataEntry("hotels", hotels_sales_sum));

        // Create the Pie chart:
        Pie pie =AnyChart.pie();

        // region pie settings:
        pie.data(seriesData);
        pie.title("Yearly sales");
        pie.labels().position("outside");

        pie.legend().title().enabled(true);
        pie.legend().title().text("Products: ").padding(0d, 0d, 0d, 0d);

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);
        // endregion pie settings:

        graph.setChart(pie);
    }


}

class CustomDataEntry extends ValueDataEntry {
    CustomDataEntry(String x, Number flights, Number attractions, Number hotels) {
        super(x, flights);
        setValue("value1", attractions);
        setValue("value2", hotels);
    }
}


