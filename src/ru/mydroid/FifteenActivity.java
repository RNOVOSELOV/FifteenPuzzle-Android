package ru.mydroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import java.util.ArrayList;
import java.util.Random;

public class FifteenActivity extends Activity {
    private static final String VALUE_SIZE = "SIZE";
    private static final String EMPTY_X = "EMPTY_SPACE_X";
    private static final String EMPTY_Y = "EMPTY_SPACE_Y";
    private static final String COUNT_STEPS = "COUNT_STEPS";
    private static final String BUTTONS_LABELS = "BUTTONS_LABELS";
    private int SIZE;
    private SquareButton[][] buttons;
    private Coordinate emptySpace;
    private OnClickListener buttonListener;
    private int countSteps;
//    private int numberOfRestarts = 0;

    Button btnRestart;
    TextView tvSteps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SIZE = 4;
        buttons = new SquareButton[SIZE][SIZE];
        emptySpace = new Coordinate();
        countSteps = 0;

        buttonListener = new OnClickListener() {
            public void onClick(View myView) {
                SquareButton clickedButton = (SquareButton) myView;
                Coordinate clickedPoint = clickedButton.coordinate;
                if (clickedPoint != null && canMove(clickedPoint)) {
                    clickedButton.setVisibility(View.INVISIBLE);
                    String numberStr = clickedButton.getText().toString();
                    clickedButton.setText("");

                    SquareButton button = buttons[emptySpace.x][emptySpace.y];
                    button.setVisibility(View.VISIBLE);
                    button.setText(numberStr);
                    if (Integer.valueOf(numberStr) == ((emptySpace.x * SIZE) + emptySpace.y + 1)) {
                        button.setBackgroundResource(R.drawable.button_correct_position);
                    } else {
                        button.setBackgroundResource(R.drawable.button_non_correct_position);
                    }

                    emptySpace.x = clickedPoint.x;
                    emptySpace.y = clickedPoint.y;
                    countSteps++;
                    tvSteps.setVisibility(View.VISIBLE);
                    tvSteps.setText(getString(R.string.text_step) + countSteps);
                    if (checkWin()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FifteenActivity.this);
                        builder.setTitle(R.string.string_win);
                        builder.setIcon(android.R.drawable.ic_dialog_info);

                        String msg = getString(R.string.string_win_message);
                        String substitutedString = String.format(msg, String.valueOf(countSteps));

                        builder.setMessage(substitutedString);
                        builder.setPositiveButton(R.string.string_positive_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog d = builder.create();
                        d.show();
                        countSteps = 0;
                        mix();
                    }
                }
            }
        };

        tvSteps = ((TextView) findViewById(R.id.textview_steps));
        btnRestart = ((Button) findViewById(R.id.button_restart));
        tuneRestartButton();
        initButtonsArray();
        tunePlayingField();
        mix();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(VALUE_SIZE, SIZE);
        outState.putInt(EMPTY_X, emptySpace.x);
        outState.putInt(EMPTY_Y, emptySpace.y);
        outState.putInt(COUNT_STEPS, countSteps);
        byte[] arr = new byte[SIZE * SIZE];
        byte index = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (buttons[i][j].getText().equals("")) {
                    arr[index++] = 0;
                } else {
                    arr[index++] = Byte.valueOf(String.valueOf(buttons[i][j].getText()));
                }
            }
        }
        outState.putByteArray(BUTTONS_LABELS, arr);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        emptySpace = new Coordinate();
        emptySpace.x = savedInstanceState.getInt(EMPTY_X);
        emptySpace.y = savedInstanceState.getInt(EMPTY_Y);
        SIZE = savedInstanceState.getInt(VALUE_SIZE);
        buttons = new SquareButton[SIZE][SIZE];
        countSteps = savedInstanceState.getInt(COUNT_STEPS);
        initButtonsArray();
        tunePlayingField();
        setLabels(savedInstanceState.getByteArray(BUTTONS_LABELS));
        if (checkWin()) {
            mix();
        } else {
            tvSteps.setVisibility(View.VISIBLE);
            tvSteps.setText(getString(R.string.text_step) + countSteps);
            acceptOnClickListener(buttonListener);
        }
    }

    private void tuneRestartButton () {
        btnRestart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                countSteps = 0;
                tvSteps.setVisibility(View.INVISIBLE);
                mixCells();
                acceptOnClickListener(buttonListener);
            }
        });
    }

    private void mix() {
        // Листенер, перемешивает ячейки и подцепляет сместо себя другой листенер, отвечающий за перемещение "подушечек"
        acceptOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mixCells();
                acceptOnClickListener(buttonListener);
            }
        });
    }

    // Проверка на победу
    private boolean checkWin() {
        if (!String.valueOf(buttons[SIZE - 1][SIZE - 1].getText()).equals("")) {
            return false;
        }
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if ((i == SIZE - 1) && (j == SIZE - 1)) {
                    continue;
                }
                try {
                    arr.add(Integer.valueOf(String.valueOf(buttons[i][j].getText())));
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        int current = arr.get(0);
        for (int index = 1; index < arr.size(); index++) {
            if (arr.get(index) < current) {
                return false;
            } else {
                current = arr.get(index);
            }
        }
        return true;
    }

    // Метод подцепляет для всех кнопок требуемый на данный момент листенер
    private void acceptOnClickListener(OnClickListener l) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j].setOnClickListener(l);
            }
        }
    }

    private boolean canMove(Coordinate clicked) {
        if (clicked.equals(emptySpace)) {
            return false;
        }
        if (clicked.x == emptySpace.x) {
            int diff = Math.abs(clicked.y - emptySpace.y);
            if (diff == 1) {
                return true;
            }
        } else if (clicked.y == emptySpace.y) {
            int diff = Math.abs(clicked.x - emptySpace.x);
            if (diff == 1) {
                return true;
            }
        }
        return false;
    }

    // Метод перемешивает "Подушечки"
    private void mixCells() {
        byte[] arr = new byte[SIZE * SIZE];
        int index = 0;
//        numberOfRestarts ++;
//        if (numberOfRestarts == 3 && SIZE == 4) {
//            arr = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0, 14, 15};
//            setLabels(arr);
//        } else {
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (buttons[i][j].getText().equals("")) {
                        arr[index++] = 0;
                    } else {
                        arr[index++] = Byte.valueOf(String.valueOf(buttons[i][j].getText()));
                    }
                }
            }
            Random rnd = new Random();
            rnd.nextInt();
            int n = arr.length;
            for (int currentIndex = 0; currentIndex < n; currentIndex++) {
                int targetIndex = currentIndex + rnd.nextInt(n - currentIndex);
                swap(arr, currentIndex, targetIndex);
            }
            if (checkState(arr, SIZE)) {
                setLabels(arr);
            } else {
                mixCells();
            }
//        }
    }


    // Меняем местами подписи у двух "подушечек" (необходимо при перемешивании)
    private static void swap(byte[] a, int i, int change) {
        byte temp = a[i];
        a[i] = a[change];
        a[change] = temp;
    }

    // Метод проверяет, решаем ли сгенеривованный расклад
    // Согласно википедии, ровно половину из всех возможных 20 922 789 888 000 (=16!)
    // начальных положений пятнашек невозможно привести к собранному виду
    // Реализован алгоритм просчета из википедии N = \sum_{i=1}^{15} n_i + e
    // https://ru.wikipedia.org/wiki/%D0%98%D0%B3%D1%80%D0%B0_%D0%B2_15
    // field - состояние игрового поля.
    // sideSize - размер стороны игрового поля.
    // return true - если можно привести к терминальному
    public boolean checkState(byte[] field, int sideSize) {
        int N = 0;
        int e = 0;
        for (int i = 0; i < field.length; i++) {    // Определяем номер ряда пустой клетки (ряд считается с 1)
            if (field[i] == 0) {
                e = i / sideSize + 1;
            }
            if (i == 0) {                           // Производится подсчет количества клеток меньших текущей
                continue;                           // на первой итерации их нет
            }
            for (int j = i + 1; j < field.length; j++) {
                if (field[j] < field[i]) {
                    N++;
                }
            }
        }
        N = N + e;              // Если N является нечётной, то решения головоломки не существует
        return (N % 2) == 0;
    }

    private void setLabels(byte[] a) {
        int index = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {

                if (a[index] == 0) {
                    buttons[i][j].setText("");
                    buttons[i][j].setVisibility(View.INVISIBLE);
                    emptySpace.x = i;
                    emptySpace.y = j;
                } else {
                    buttons[i][j].setText(String.valueOf(a[index]));
                    buttons[i][j].setVisibility(View.VISIBLE);
                    if (a[index] == index + 1) {
                        buttons[i][j].setBackgroundResource(R.drawable.button_correct_position);
                    } else {
                        buttons[i][j].setBackgroundResource(R.drawable.button_non_correct_position);
                    }
                }
                index++;
            }
        }
    }

    // Конфигурирование игрового поля при запуске игрульки
    private void tunePlayingField() {
        byte[] arr = new byte[SIZE * SIZE];
        int index = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                SquareButton button = buttons[i][j];
                button.coordinate.x = i;
                button.coordinate.y = j;
                if (index >= SIZE * SIZE - 1) {
                    arr[index] = 0;
                    button.setVisibility(View.INVISIBLE);
                    emptySpace.x = i;
                    emptySpace.y = j;
                } else {
                    arr[index] = (byte) (index + 1);
                }
                index++;
            }
        }
        setLabels(arr);
    }

    private void initButtonsArray() {
        String custom_font = "fonts/fifteen_puzzle_font.ttf";
        Typeface CF = Typeface.createFromAsset(getAssets(), custom_font);
        TableLayout mainTV = (TableLayout) findViewById(R.id.table_for_insert_buttons);
        for (int i = 0; i < SIZE; i++) {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            for (int j = 0; j < SIZE; j++) {
                SquareButton squareButton = new SquareButton(this);
                squareButton.setLayoutParams(new TableRow.LayoutParams(0, 0, 1));
                buttons[i][j] = squareButton;
                buttons[i][j].setTypeface(CF);
                tr.addView(squareButton, j);
            }
            mainTV.addView(tr, i);
        }
    }

    public void showSettingsDialog(View view) {
        final View customView = getLayoutInflater().inflate(R.layout.dialog, null);
        final NumberPicker np = ((NumberPicker) customView.findViewById(R.id.numberPicker));
        np.setMinValue(3);
        np.setMaxValue(6);
        np.setValue(4);
        new AlertDialog.Builder(this)
                .setView(customView)
                .setPositiveButton(R.string.string_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SIZE = np.getValue();
                        buttons = new SquareButton[SIZE][SIZE];
                        emptySpace = new Coordinate();
                        countSteps = 0;
                        TableLayout mainTV = (TableLayout) findViewById(R.id.table_for_insert_buttons);
                        mainTV.removeAllViewsInLayout();
                        tuneRestartButton();
                        initButtonsArray();
                        tunePlayingField();
                        mix();
                    }
                })
                .setTitle(R.string.button_settings)
                .create()
                .show();

    }
}