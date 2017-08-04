package graphics.panels;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import graphics.GameBorder;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1244543786097347429L;
	private TetrisGraphics tetrisGraphics;

	public GamePanel(TetrisGraphics tetrisGraphics) {
		this.tetrisGraphics = tetrisGraphics;
		JPanel grid = new GridPanel();
		this.setLayout(null);
		grid.setBounds(10, 10, 391, 859);
		this.add(grid);
		defineGamePanel();
	}

	private void defineGamePanel() {
		this.setBounds(20, 58, 411, 878);
		this.setBackground(new Color(0, 0, 0));
		this.setBorder(new GameBorder());
	}

	public TetrisGraphics getTetrisGraphics() {
		return tetrisGraphics;
	}

}
