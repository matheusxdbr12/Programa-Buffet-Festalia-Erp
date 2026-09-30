package com.festalia;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BuffetErpApp extends JFrame {

    private List<Cardapio> listaCardapios = new ArrayList<>();
    private JComboBox<Cardapio> cbCardapios;
    
    private JTextField txtCliente, txtTelefone, txtLocal, txtConvidados;
    private JTextArea txtObservacoes;

    public BuffetErpApp() {
        setTitle("Festalia ERP - Gestão Integrada de Buffets e Eventos");
        setSize(600, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarDadosExemplo();
        montarTela();
    }

    private void inicializarDadosExemplo() {
        Cardapio c1 = new Cardapio("Casamento Premium", "Menu completo para festas noturnas");
        c1.adicionarItem("Entrada: Canapés variados e Salgados finos");
        c1.adicionarItem("Prato Principal: Filet Mignon ao molho madeira e Risoto de Gorgonzola");
        c1.adicionarItem("Sobremesa: Petit Gâteau com Sorvete de Baunilha");
        c1.adicionarItem("Bebidas: Água, Refrigerante, Cerveja e Vinho Fino");

        Cardapio c2 = new Cardapio("Infantil Divertido", "Opção voltada para festas de aniversário infantil");
        c2.adicionarItem("Salgados: Coxinha, Empada, Bolinha de Queijo");
        c2.adicionarItem("Prato Principal: Mini-Hambúrguer e Pizza Brotinho");
        c2.adicionarItem("Sobremesa: Bolo de Brigadeiro e Sorvete");
        c2.adicionarItem("Bebidas: Sucos Naturais, Água e Refrigerante");

        listaCardapios.add(c1);
        listaCardapios.add(c2);
    }

    private void montarTela() {
        JPanel panel = new JPanel(new GridLayout(8, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(new JLabel("Nome do Cliente:"));
        txtCliente = new JTextField();
        panel.add(txtCliente);

        panel.add(new JLabel("Telefone:"));
        txtTelefone = new JTextField();
        panel.add(txtTelefone);

        panel.add(new JLabel("Local do Evento:"));
        txtLocal = new JTextField();
        panel.add(txtLocal);

        panel.add(new JLabel("Qtd. de Convidados:"));
        txtConvidados = new JTextField();
        panel.add(txtConvidados);

        panel.add(new JLabel("Cardápio:"));
        cbCardapios = new JComboBox<>(listaCardapios.toArray(new Cardapio[0]));
        panel.add(cbCardapios);

        panel.add(new JLabel("Observações:"));
        txtObservacoes = new JTextArea(3, 20);
        JScrollPane scrollObs = new JScrollPane(txtObservacoes);
        panel.add(scrollObs);

        JButton btnGerarPDF = new JButton("Gerar Ordem de Serviço em PDF");
        btnGerarPDF.addActionListener(e -> gerarOS());

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(btnGerarPDF, BorderLayout.SOUTH);
    }

    private void gerarOS() {
        try {
            String cliente = txtCliente.getText();
            String telefone = txtTelefone.getText();
            String local = txtLocal.getText();
            int convidados = Integer.parseInt(txtConvidados.getText());
            Cardapio cardapio = (Cardapio) cbCardapios.getSelectedItem();
            String obs = txtObservacoes.getText();

            OrdemServico os = new OrdemServico(cliente, telefone, LocalDate.now(), local, convidados, cardapio, obs);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File("Festalia_OS_" + os.getNumeroOS() + ".pdf"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                String caminho = fileChooser.getSelectedFile().getAbsolutePath();
                PDFGenerator.gerarOrdemServicoPDF(os, caminho);
                JOptionPane.showMessageDialog(this, "Ordem de Serviço gerada com sucesso pelo Festalia ERP!\nSalvo em: " + caminho);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, informe um número válido para a quantidade de convidados.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao gerar PDF: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BuffetErpApp().setVisible(true));
    }
}