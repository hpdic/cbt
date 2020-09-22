//https://blog.csdn.net/weixin_33807284/article/details/90228051
package com.nvada.blocklite.frame;

import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
 
import javax.swing.JFrame;
 
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;
 
public class BarChartDemo {
 
    private ChartPanel panel;
 
    /**
     * 在柱状图中创建表格的步骤如下： 1、创建表格chart,需要注意相关参数的含义， 2、传进去的数据集是CategoryDataset格式
     * 3、获得表格区域块，设置横轴，纵轴及相关字体（防止出现乱卡的状况）
     * 4、设置chart的图例legend，并设置条目的字体格式（同样是为了防止出现乱码）
     */
    public BarChartDemo() {
        CategoryDataset dataset = (CategoryDataset) getDataset();
        JFreeChart chart = ChartFactory.createBarChart3D("学历信息统计", "横坐标",
                "纵坐标", dataset, PlotOrientation.VERTICAL, true, false, false);
 
        CategoryPlot plot = (CategoryPlot) chart.getCategoryPlot();
        CategoryAxis axis = plot.getDomainAxis();
        axis.setLabelFont(new Font("宋体", Font.BOLD, 20));
        axis.setTickLabelFont(new Font("宋体", Font.BOLD, 20));
 
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelFont(new Font("宋体", Font.BOLD, 20));
 
        chart.getLegend().setItemFont(new Font("宋体", Font.BOLD, 20));
        chart.getTitle().setFont(new Font("黑体", Font.ITALIC, 22));
 
        panel = new ChartPanel(chart, true);
 
        
    }
    
    public void save2png(JFreeChart chart) {
        File dir = new File("resources/");   
        
        if (!dir.exists()) {  
            dir.mkdir();  
        }
        
        String fName = String.valueOf(System.currentTimeMillis())+"BarChart.png";  
        File file = new File("resources/", fName);
        
        try {
            ChartUtilities.saveChartAsPNG(file, chart, 400, 250);
        } catch (IOException e) {
            e.printStackTrace();
        }  
    }
 
    public ChartPanel getChartPanel() {
        return panel;
    }
 
    /**
     * 需要注意的是在向数据集中添加数据的时候 使用的是dataset.addValue()方法，而在饼状图的数据集添加数据的过程中，使用的是dataset.setValue()方法
     * 这一点应该尤其注意。以免出错！
     * @return
     */
    private static Dataset getDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(1, "郭1", "大学");
        dataset.addValue(2, "郭2", "高中");
        dataset.addValue(3, "郭3", "高中");
        dataset.addValue(4, "郭4", "高中");
        dataset.addValue(5, "郭5", "初中");
        dataset.addValue(5, "郭6", "初中");
        dataset.addValue(4, "郭7", "初中");
        dataset.addValue(3, "郭8", "小学");
        dataset.addValue(2, "郭9", "幼儿园");
        dataset.addValue(1, "张10", "幼儿园");
 
        return dataset;
    }
 
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.add(new BarChartDemo().getChartPanel());
        frame.setSize(1000, 600);
        frame.setDefaultCloseOperation(0);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
 
}


