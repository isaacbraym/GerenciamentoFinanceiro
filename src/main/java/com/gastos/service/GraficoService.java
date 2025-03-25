package com.gastos.service;

import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Serviço para criação e gerenciamento de gráficos.
 */
public class GraficoService {
    
    /**
     * Cria um gráfico de pizza com os dados fornecidos.
     * 
     * @param titulo Título do gráfico
     * @param dados Lista de dados no formato [nome, valor]
     * @return JFreeChart configurado
     */
    @SuppressWarnings("unchecked")
    public JFreeChart criarGraficoPizza(String titulo, List<Object[]> dados) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        for (Object[] dado : dados) {
            String categoria = (String) dado[0];
            double valor = (double) dado[1];
            dataset.setValue(categoria, valor);
        }

        JFreeChart chart = ChartFactory.createPieChart(titulo, dataset, true, true, false);

        // Personalizar o gráfico
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint((java.awt.Paint) java.awt.Color.WHITE);
        plot.setOutlinePaint(null);

        return chart;
    }
    
    /**
     * Cria um visualizador de gráfico a partir de um JFreeChart.
     * 
     * @param chart O gráfico JFreeChart
     * @param largura Largura do visualizador
     * @param altura Altura do visualizador
     * @return ChartViewer configurado
     */
    public ChartViewer criarVisualizadorGrafico(JFreeChart chart, double largura, double altura) {
        ChartViewer viewer = new ChartViewer(chart);
        viewer.setPrefSize(largura, altura);
        return viewer;
    }
    
    /**
     * Cria um gráfico de pizza e seu visualizador em uma única chamada.
     * 
     * @param titulo Título do gráfico
     * @param dados Lista de dados no formato [nome, valor]
     * @param largura Largura do visualizador
     * @param altura Altura do visualizador
     * @return ChartViewer configurado com o gráfico
     */
    public ChartViewer criarGraficoPizzaComVisualizador(String titulo, List<Object[]> dados, 
                                                      double largura, double altura) {
        JFreeChart chart = criarGraficoPizza(titulo, dados);
        return criarVisualizadorGrafico(chart, largura, altura);
    }
}