package com.festalia.util;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.function.Consumer;

/**
 * Fábrica de Ícones Vetoriais Antialiased de Alta Resolução do Festalia ERP.
 * Substitui emojis e caracteres unicode que frequentemente causam tofu / "quadrados pretos"
 * nos sistemas Windows por ícones nítidos, modernos e compatíveis em qualquer tela/resolução.
 */
public final class IconesFestalia {

    private IconesFestalia() {}

    /**
     * Ícone de Alerta Triangular com Exclamação (para avisos de conflito).
     */
    public static Icon alerta(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;

            // Triângulo com pontas levemente arredondadas
            Path2D.Float p = new Path2D.Float();
            p.moveTo(w * 0.5f, h * 0.1f);
            p.lineTo(w * 0.92f, h * 0.88f);
            p.quadTo(w * 0.95f, h * 0.95f, w * 0.86f, h * 0.95f);
            p.lineTo(w * 0.14f, h * 0.95f);
            p.quadTo(w * 0.05f, h * 0.95f, w * 0.08f, h * 0.88f);
            p.closePath();
            g2.fill(p);

            // Exclamação vazada (cor invertida ou de fundo)
            g2.setColor(Color.WHITE);
            float strokeW = Math.max(1.8f, w * 0.12f);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(w * 0.5f, h * 0.42f, w * 0.5f, h * 0.65f));

            float dotR = Math.max(1.5f, w * 0.07f);
            g2.fill(new Ellipse2D.Float(w * 0.5f - dotR, h * 0.80f - dotR, dotR * 2, dotR * 2));
        });
    }

    /**
     * Ícone de Alerta Destaque Grande para o Topo dos Diálogos.
     */
    public static Icon alertaGrande(int tamanho) {
        return criarIcone(tamanho, tamanho, g2 -> {
            float w = tamanho;
            float h = tamanho;

            // Fundo circular com tom suave de atenção
            g2.setColor(new Color(254, 215, 170)); // Amber claro
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, w * 0.35f, h * 0.35f));

            // Borda suave
            g2.setColor(new Color(251, 146, 60));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, w - 1.5f, h - 1.5f, w * 0.35f, h * 0.35f));

            // Triângulo centralizado
            float tw = w * 0.65f;
            float th = h * 0.60f;
            float tx = (w - tw) / 2f;
            float ty = (h - th) / 2f + 1;

            Path2D.Float p = new Path2D.Float();
            p.moveTo(tx + tw * 0.5f, ty);
            p.lineTo(tx + tw, ty + th);
            p.lineTo(tx, ty + th);
            p.closePath();

            g2.setColor(new Color(217, 119, 6)); // Amber escuro
            g2.fill(p);

            // Ponto de exclamação branco
            g2.setColor(Color.WHITE);
            float strokeW = Math.max(2.2f, w * 0.08f);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(w * 0.5f, ty + th * 0.35f, w * 0.5f, ty + th * 0.65f));

            float dotR = Math.max(1.8f, w * 0.045f);
            g2.fill(new Ellipse2D.Float(w * 0.5f - dotR, ty + th * 0.82f - dotR, dotR * 2, dotR * 2));
        });
    }

    /**
     * Ícone de Cliente / Usuário (Silhueta profissional de perfil).
     */
    public static Icon cliente(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;

            // Cabeça
            float headR = w * 0.22f;
            g2.fill(new Ellipse2D.Float(w * 0.5f - headR, h * 0.12f, headR * 2, headR * 2));

            // Ombros
            Path2D.Float ombros = new Path2D.Float();
            ombros.moveTo(w * 0.18f, h * 0.88f);
            ombros.curveTo(w * 0.18f, h * 0.62f, w * 0.32f, h * 0.58f, w * 0.5f, h * 0.58f);
            ombros.curveTo(w * 0.68f, h * 0.58f, w * 0.82f, h * 0.62f, w * 0.82f, h * 0.88f);
            ombros.closePath();
            g2.fill(ombros);
        });
    }

    /**
     * Ícone de Relógio / Horário.
     */
    public static Icon horario(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;

            float pad = w * 0.10f;
            float strokeW = Math.max(1.5f, w * 0.09f);
            g2.setStroke(new BasicStroke(strokeW));
            g2.draw(new Ellipse2D.Float(pad, pad, w - pad * 2, h - pad * 2));

            // Ponteiros: hora e minuto (ex: 3:00)
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(w * 0.5f, h * 0.5f, w * 0.5f, h * 0.26f));
            g2.draw(new Line2D.Float(w * 0.5f, h * 0.5f, w * 0.74f, h * 0.5f));
        });
    }

    /**
     * Ícone de Local / Marcador de Mapa (Pin).
     */
    public static Icon local(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;

            float cx = w * 0.5f;
            float cy = h * 0.40f;
            float r = w * 0.30f;

            Path2D.Float pin = new Path2D.Float();
            pin.moveTo(cx, h * 0.94f);
            pin.curveTo(cx - r * 0.8f, cy + r * 0.6f, cx - r, cy + r * 0.2f, cx - r, cy);
            pin.curveTo(cx - r, cy - r, cx + r, cy - r, cx + r, cy);
            pin.curveTo(cx + r, cy + r * 0.2f, cx + r * 0.8f, cy + r * 0.6f, cx, h * 0.94f);
            pin.closePath();
            g2.fill(pin);

            // Orifício central branco
            g2.setColor(Color.WHITE);
            float dotR = r * 0.42f;
            g2.fill(new Ellipse2D.Float(cx - dotR, cy - dotR, dotR * 2, dotR * 2));
        });
    }

    /**
     * Ícone de Convidados / Pessoas (2 silhuetas).
     */
    public static Icon convidados(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;

            // Pessoa de trás / secundária (à direita)
            float h2R = w * 0.16f;
            g2.fill(new Ellipse2D.Float(w * 0.68f - h2R, h * 0.18f, h2R * 2, h2R * 2));
            Path2D.Float ombros2 = new Path2D.Float();
            ombros2.moveTo(w * 0.46f, h * 0.85f);
            ombros2.curveTo(w * 0.46f, h * 0.66f, w * 0.55f, h * 0.60f, w * 0.68f, h * 0.60f);
            ombros2.curveTo(w * 0.82f, h * 0.60f, w * 0.90f, h * 0.66f, w * 0.90f, h * 0.85f);
            ombros2.closePath();
            g2.fill(ombros2);

            // Pessoa principal (à frente e à esquerda)
            g2.setColor(cor);
            float h1R = w * 0.19f;
            g2.fill(new Ellipse2D.Float(w * 0.38f - h1R, h * 0.15f, h1R * 2, h1R * 2));
            Path2D.Float ombros1 = new Path2D.Float();
            ombros1.moveTo(w * 0.10f, h * 0.88f);
            ombros1.curveTo(w * 0.10f, h * 0.63f, w * 0.22f, h * 0.57f, w * 0.38f, h * 0.57f);
            ombros1.curveTo(w * 0.54f, h * 0.57f, w * 0.66f, h * 0.63f, w * 0.66f, h * 0.88f);
            ombros1.closePath();
            g2.fill(ombros1);
        });
    }

    /**
     * Ícone de Cancelar (✕ em círculo ou X limpo).
     */
    public static Icon cancelar(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;
            float pad = w * 0.22f;
            float strokeW = Math.max(1.8f, w * 0.14f);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(pad, pad, w - pad, h - pad));
            g2.draw(new Line2D.Float(w - pad, pad, pad, h - pad));
        });
    }

    /**
     * Ícone de Voltar / Retorno (seta para a esquerda ou relógio de ajuste).
     */
    public static Icon voltar(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;
            float strokeW = Math.max(1.8f, w * 0.13f);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Seta para esquerda com curvatura
            float midY = h * 0.5f;
            g2.draw(new Line2D.Float(w * 0.25f, midY, w * 0.82f, midY));
            g2.draw(new Line2D.Float(w * 0.25f, midY, w * 0.50f, midY - h * 0.25f));
            g2.draw(new Line2D.Float(w * 0.25f, midY, w * 0.50f, midY + h * 0.25f));
        });
    }

    /**
     * Ícone de Confirmar / Checkmark.
     */
    public static Icon confirmar(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;
            float strokeW = Math.max(1.8f, w * 0.14f);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Float p = new Path2D.Float();
            p.moveTo(w * 0.18f, h * 0.52f);
            p.lineTo(w * 0.42f, h * 0.78f);
            p.lineTo(w * 0.84f, h * 0.25f);
            g2.draw(p);
        });
    }

    /**
     * Ícone de Calendário (Folha de agenda com grade).
     */
    public static Icon calendario(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;

            float padX = w * 0.12f;
            float padY = h * 0.16f;
            float strokeW = Math.max(1.5f, w * 0.09f);

            // Contorno do calendário
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(padX, padY, w - padX * 2, h - padY * 1.5f, 3, 3));

            // Faixa superior
            g2.draw(new Line2D.Float(padX, padY + h * 0.22f, w - padX, padY + h * 0.22f));

            // Pinos de suspensão
            g2.draw(new Line2D.Float(w * 0.32f, padY - h * 0.08f, w * 0.32f, padY + h * 0.08f));
            g2.draw(new Line2D.Float(w * 0.68f, padY - h * 0.08f, w * 0.68f, padY + h * 0.08f));

            // Pontinhos de grade
            float dotR = Math.max(1.0f, w * 0.04f);
            g2.fill(new Ellipse2D.Float(w * 0.33f - dotR, h * 0.55f - dotR, dotR * 2, dotR * 2));
            g2.fill(new Ellipse2D.Float(w * 0.50f - dotR, h * 0.55f - dotR, dotR * 2, dotR * 2));
            g2.fill(new Ellipse2D.Float(w * 0.67f - dotR, h * 0.55f - dotR, dotR * 2, dotR * 2));

            g2.fill(new Ellipse2D.Float(w * 0.33f - dotR, h * 0.72f - dotR, dotR * 2, dotR * 2));
            g2.fill(new Ellipse2D.Float(w * 0.50f - dotR, h * 0.72f - dotR, dotR * 2, dotR * 2));
            g2.fill(new Ellipse2D.Float(w * 0.67f - dotR, h * 0.72f - dotR, dotR * 2, dotR * 2));
        });
    }

    /**
     * Ícone de PDF / Documento.
     */
    public static Icon pdf(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;
            float padX = w * 0.18f;
            float padY = h * 0.12f;
            float strokeW = Math.max(1.5f, w * 0.09f);

            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Folha com dobra no canto superior direito
            float fold = w * 0.25f;
            Path2D.Float p = new Path2D.Float();
            p.moveTo(padX, padY);
            p.lineTo(w - padX - fold, padY);
            p.lineTo(w - padX, padY + fold);
            p.lineTo(w - padX, h - padY);
            p.lineTo(padX, h - padY);
            p.closePath();
            g2.draw(p);

            // Linha da dobra
            g2.draw(new Line2D.Float(w - padX - fold, padY, w - padX - fold, padY + fold));
            g2.draw(new Line2D.Float(w - padX - fold, padY + fold, w - padX, padY + fold));

            // Linhas de texto
            g2.draw(new Line2D.Float(padX + w * 0.12f, h * 0.52f, w - padX - w * 0.12f, h * 0.52f));
            g2.draw(new Line2D.Float(padX + w * 0.12f, h * 0.68f, w - padX - w * 0.12f, h * 0.68f));
        });
    }

    /**
     * Ícone de Editar / Lápis.
     */
    public static Icon editar(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;
            float strokeW = Math.max(1.6f, w * 0.11f);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Traço inclinado do lápis
            g2.draw(new Line2D.Float(w * 0.20f, h * 0.80f, w * 0.75f, h * 0.25f));
            // Ponta
            g2.draw(new Line2D.Float(w * 0.18f, h * 0.82f, w * 0.15f, h * 0.85f));
            // Base
            g2.draw(new Line2D.Float(w * 0.15f, h * 0.90f, w * 0.35f, h * 0.90f));
        });
    }

    /**
     * Ícone de Estrela (para reservas realizadas).
     */
    public static Icon estrela(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float cx = tamanho * 0.5f;
            float cy = tamanho * 0.5f;
            float rExt = tamanho * 0.44f;
            float rInt = rExt * 0.45f;

            Path2D.Float p = new Path2D.Float();
            for (int i = 0; i < 10; i++) {
                double ang = i * Math.PI / 5 - Math.PI / 2;
                float r = (i % 2 == 0) ? rExt : rInt;
                float px = (float) (cx + Math.cos(ang) * r);
                float py = (float) (cy + Math.sin(ang) * r);
                if (i == 0) p.moveTo(px, py);
                else p.lineTo(px, py);
            }
            p.closePath();
            g2.fill(p);
        });
    }

    /**
     * Ícone de Mais / Adicionar (➕).
     */
    public static Icon mais(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;
            float strokeW = Math.max(1.8f, w * 0.15f);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Float(w * 0.5f, h * 0.18f, w * 0.5f, h * 0.82f));
            g2.draw(new Line2D.Float(w * 0.18f, h * 0.5f, w * 0.82f, h * 0.5f));
        });
    }

    /**
     * Ícone de Cardápio / Menu (Garfo e Faca estilizados ou lista).
     */
    public static Icon cardapio(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;
            float strokeW = Math.max(1.5f, w * 0.09f);
            g2.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Prato / Cloche
            g2.draw(new Arc2D.Float(w * 0.15f, h * 0.28f, w * 0.70f, h * 0.44f, 0, 180, Arc2D.OPEN));
            g2.draw(new Line2D.Float(w * 0.10f, h * 0.52f, w * 0.90f, h * 0.52f));
            // Puxador
            g2.draw(new Line2D.Float(w * 0.5f, h * 0.22f, w * 0.5f, h * 0.28f));
            // Base
            g2.draw(new Line2D.Float(w * 0.22f, h * 0.70f, w * 0.78f, h * 0.70f));
        });
    }

    /**
     * Ícone de Moeda / Financeiro (Cifrão dentro de um círculo nítido).
     */
    public static Icon moeda(int tamanho, Color cor) {
        return criarIcone(tamanho, tamanho, g2 -> {
            g2.setColor(cor);
            float w = tamanho;
            float h = tamanho;
            float strokeW = Math.max(1.5f, w * 0.09f);

            // Círculo externo
            g2.setStroke(new BasicStroke(strokeW));
            g2.draw(new Ellipse2D.Float(w * 0.10f, h * 0.10f, w * 0.80f, h * 0.80f));

            // Cifrão central
            Path2D.Float s = new Path2D.Float();
            s.moveTo(w * 0.65f, h * 0.35f);
            s.curveTo(w * 0.45f, h * 0.25f, w * 0.35f, h * 0.40f, w * 0.50f, h * 0.50f);
            s.curveTo(w * 0.65f, h * 0.60f, w * 0.55f, h * 0.75f, w * 0.35f, h * 0.65f);
            g2.draw(s);

            // Haste vertical
            g2.draw(new Line2D.Float(w * 0.50f, h * 0.20f, w * 0.50f, h * 0.80f));
        });
    }

    private static Icon criarIcone(int w, int h, Consumer<Graphics2D> renderizador) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                    g2.translate(x, y);
                    renderizador.accept(g2);
                } finally {
                    g2.dispose();
                }
            }

            @Override
            public int getIconWidth() {
                return w;
            }

            @Override
            public int getIconHeight() {
                return h;
            }
        };
    }
}
