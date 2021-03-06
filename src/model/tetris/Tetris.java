package model.tetris;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.Observable;
import java.util.Timer;

import common.Action;
import model.tetromino.Tetromino;
import model.tetromino.TetrominoGenerator;

public class Tetris extends Observable {

	private Color[][] grid;
	private int width = 10;
	private int height = 22;
	private int difficulty;

	// Tetromino fields
	private TetrominoGenerator generator = new TetrominoGenerator();
	private Tetromino current;
	private Tetromino[] nextQueue;
	private Tetromino heldTetromino;
	private Point[] oldPosition;
	private int tetrominoCount = 0;

	public Tetris(int difficulty) {

		grid = new Color[width][height];
		this.difficulty = difficulty;

		// Define and fill the queue of next tetrominoes
		nextQueue = new Tetromino[4];
		for (int i = 0; i < nextQueue.length; i++) {
			nextQueue[i] = getTetromino();
		}

		createTetromino();
		run();
	}

	private void run() {

		Timer t = new Timer();
		TetrisTask tt = new TetrisTask(this);
		t.scheduleAtFixedRate(tt, 1000, 1000 / difficulty);
	}

	private void createTetromino() {

		current = nextQueue[0];
		for (int i = 0; i < nextQueue.length - 1; i++) {
			nextQueue[i] = nextQueue[i + 1];
		}
		nextQueue[nextQueue.length - 1] = getTetromino();

		restartPosition();

	}

	private void restartPosition() {

		// Set original coordinates for tetromino
		current.setX(grid.length / 2);
		current.setY(0);

		// Add all points for the old position
		defineOldPosition();

		updateGrid();
	}

	private void defineOldPosition() {

		oldPosition = new Point[4];
		boolean[][] tetromino = current.getTetrominoMatrix(current.getOrientation());
		int count = 0;
		for (int x = 0; x < tetromino.length; x++) {
			for (int y = 0; y < tetromino[0].length; y++) {
				if (tetromino[x][y]) {
					oldPosition[count] = new Point((current.getX() - tetromino.length / 2) + x, current.getY() + y);
					count++;
				}
			}
		}
	}

	public Tetromino getTetromino() {

		List<Tetromino> tetrominoes = generator.getTetrominoes();
		// Restart using the same tetromino set
		if (tetrominoCount >= tetrominoes.size() - 1) {
			tetrominoCount = 0;
		}
		return tetrominoes.get(tetrominoCount++);
	}

	public void holdTetromino() {

		if (heldTetromino == null) {
			heldTetromino = current;
			heldTetromino.setX(grid.length / 2);
			heldTetromino.setY(0);

			current = nextQueue[0];
			nextQueue[nextQueue.length - 1] = getTetromino();
		} else {
			Tetromino temp = current;
			current = heldTetromino;
			heldTetromino = temp;

			heldTetromino.setX(grid.length / 2);
			heldTetromino.setY(0);
			current.setX(grid.length / 2);
			current.setY(0);

		}
		clearAndUpdateOldPosition();
	}

	public void setTetrominoAction(Action action) {

		boolean success = true;

		switch (action) {
		case ROTATE_LEFT:
			// TODO: Correct the range checks here
			if (rangeCheckX(1)) {
				current.rotateLeft();
			} else {
				success = false;
			}
			break;
		case ROTATE_RIGHT:
			// TODO: Correct the range checks here
			if (rangeCheckX(1)) {
				current.rotateRight();
			} else {
				success = false;
			}
			break;
		case MOVE_LEFT:
			if (rangeCheckX(-1)) {
				current.moveLeft();
			} else {
				success = false;
			}
			break;
		case MOVE_RIGHT:
			if (rangeCheckX(1)) {
				current.moveRight();
			} else {
				success = false;
			}
			break;
		case SOFT_DROP:
			if (rangeCheckY(1)) {
				current.softDrop();
			} else {
				createTetromino();
			}
			break;
		case HARD_DROP:
			current.hardDrop();
			break;
		default:
			break;
		}
		if (success) {
			updateGrid();
		}
	}

	private boolean rangeCheckX(int changeX) {

		int result = current.getX() + changeX;
		boolean[][] tetrominoMatrix = current.getTetrominoMatrix(current.getOrientation());
		if (changeX > 0) {
			result += tetrominoMatrix.length / 2 + tetrominoMatrix.length % 2;
		} else if (changeX < 0) {
			result -= tetrominoMatrix.length / 2;
		}

		// Check out of bounds
		if (result < 0 || result > grid.length) {
			return false;
		}

		// Check collision
		for (Point p : oldPosition) {

			Color gridPosition = grid[p.x + changeX][p.y];
			// Check if the new point is filled
			if (gridPosition != null) {

				// Check if the new point is part of an old position for this tetromino
				boolean found = false;
				for (Point old : oldPosition) {
					if (old.equals(new Point(p.x + changeX, p.y))) {
						found = true;
					}
				}

				if (!found) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean rangeCheckY(int changeY) {

		if (placeTetromino(changeY)) {
			rowClear();
			return false;
		}

		return true;
	}

	private boolean placeTetromino(int changeY) {

		boolean[][] tetrominoMatrix = current.getTetrominoMatrix(current.getOrientation());
		int result = current.getY() + tetrominoMatrix[0].length + changeY;
		int endX = current.getX() + tetrominoMatrix.length / 2 + tetrominoMatrix.length % 2;

		// Find any square at the bottom of the tetromino, return true if placed
		for (int gridX = current.getX() - tetrominoMatrix.length / 2; gridX < endX; gridX++) {
			int tetrominoX = gridX - (current.getX() - tetrominoMatrix.length / 2);

			for (int gridY = current.getY(); gridY < result - 1; gridY++) {
				int tetrominoY = gridY - current.getY();

				// Continue if the square underneath is part of this tetromino
				if (tetrominoY + 1 < tetrominoMatrix[0].length && tetrominoMatrix[tetrominoX][tetrominoY + 1]) {
					continue;
				}

				// Continue if this square is not part of this tetromino
				if (!tetrominoMatrix[tetrominoX][tetrominoY]) {
					continue;
				}

				try {
					if (grid[gridX][gridY + 1] != null) {
						return true;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					return true;
				}
			}
		}
		return false;
	}

	private void rowClear() {

		for (int y = 0; y < grid[0].length; y++) {
			for (int x = 0; x < grid.length; x++) {
				// Checking horizontally along each row
				if (grid[x][y] == null) {
					break;
				}
				if (x == grid.length - 1) {
					clearRow(y);
				}
			}
		}

	}

	private void clearRow(int toClear) {

		for (int y = toClear; y > 1; y--) {
			for (int x = 0; x < grid.length; x++) {
				grid[x][y] = grid[x][y - 1];
			}
		}
	}

	private void updateGrid() {

		clearAndUpdateOldPosition();

		// Update tetromino in new position
		boolean[][] tetromino = current.getTetrominoMatrix(current.getOrientation());
		for (int i = 0; i < tetromino.length; i++) {
			for (int j = 0; j < tetromino[0].length; j++) {
				if (tetromino[i][j]) {
					int actualX = current.getX() - (tetromino.length / 2) + i;
					int actualY = current.getY() + j;
					grid[actualX][actualY] = current.getColor();
				}
			}
		}
		setChanged();
		notifyObservers();
	}

	private void clearAndUpdateOldPosition() {

		// Clear the tiles in the old position
		for (Point p : oldPosition) {
			grid[p.x][p.y] = null;
		}
		// Update old position to be current
		defineOldPosition();
	}

	public TetrominoGenerator getGenerator() {
		return generator;
	}

	public Color[][] getGrid() {
		return grid;
	}

	public void setGrid(Color[][] grid) {
		this.grid = grid;
	}

	public Tetromino getCurrent() {
		return current;
	}

	public Tetromino[] getNextQueue() {
		return nextQueue;
	}

	public Tetromino getHeldTetromino() {
		return heldTetromino;
	}
}
