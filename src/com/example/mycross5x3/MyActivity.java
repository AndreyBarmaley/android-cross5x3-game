/***************************************************************************
 *   Copyright (C) 2018 by AndreyBarmaley  <public.irkutsk@gmail.com>      *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 3 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package com.example.mycross5x3;

import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.mycross5x2.R;

public class MyActivity extends Activity {
	enum ButtonState { Empty, AI, Human };
	static int winsX = 0;
	static int winsO = 0;
	static int winVariant = -1;
	static boolean humanFirst = false;
	static boolean disableAI = false;
	static boolean allowReset = false;
	static int buttonsId[] = { R.id.imageView0x0, R.id.imageView1x0, R.id.imageView2x0, R.id.imageView3x0, R.id.imageView4x0,
								R.id.imageView0x1, R.id.imageView1x1, R.id.imageView2x1, R.id.imageView3x1, R.id.imageView4x1,
								R.id.imageView0x2, R.id.imageView1x2, R.id.imageView2x2, R.id.imageView3x2, R.id.imageView4x2 };
	static Map<Integer, ButtonState> buttonsState = null;
	static List<int[]> winVariants = null;
	static List<Integer> history;
	static Vibrator vibra = null;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setContentView(R.layout.my_activity);
		
		if(buttonsState == null) {
			vibra = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			initData();
			startGame();
		}

		updateButtons();
		updateScore();
	}

	@Override
    public void onDestroy() {
		super.onDestroy();
		saveData();
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menuExit) {
			finish();
		}
		return true;
	}
	
	private void updateButtons() {
		for(int yy = 0; yy < 3; ++yy) {
			for(int xx = 0; xx < 5; ++xx) {
				int buttonId = buttonsId[getIndex(xx, yy)];
				ImageView image = (ImageView) findViewById(buttonId);
				if(image != null) {
					ButtonState state = buttonsState.get(buttonId);
					image.setImageResource(getResourceState(state, false));
					image.setEnabled(state == ButtonState.Empty);
				}
			}
		}
	}
	
	private void resetButtons() {
		for(int id : buttonsId) {
			ImageView image = (ImageView) findViewById(id);
			if(image != null) {
				image.setImageResource(getResourceState(ButtonState.Empty, false));
				setButtonState(id, ButtonState.Empty);
				image.setEnabled(true);
			}
		}
	}

	private void enableButtons(boolean f) {
		for(int id : buttonsId) {
			ImageView image = (ImageView) findViewById(id);
			
			if(image != null) {
				image.setEnabled(f);
			}
		}

	}

	private int getIndex(int xx, int yy) {
		return yy * 5 + xx;
	}

	private void initWinVariants() {
		winVariants = new ArrayList<int[]>();
		
	    int[] ldiagonal1 = { R.id.imageView0x0, R.id.imageView1x1, R.id.imageView2x2 };
	    winVariants.add(ldiagonal1);

	    int[] ldiagonal2 = { R.id.imageView1x0, R.id.imageView2x1, R.id.imageView3x2 };
	    winVariants.add(ldiagonal2);
	    
	    int[] ldiagonal3 = { R.id.imageView2x0, R.id.imageView3x1, R.id.imageView4x2 };
	    winVariants.add(ldiagonal3);

	    int[] rdiagonal1 = { R.id.imageView2x0, R.id.imageView1x1, R.id.imageView0x2 };
	    winVariants.add(rdiagonal1);
	    
	    int[] rdiagonal2 = { R.id.imageView3x0, R.id.imageView2x1, R.id.imageView1x2 };
	    winVariants.add(rdiagonal2);
	    
	    int[] rdiagonal3 = { R.id.imageView4x0, R.id.imageView3x1, R.id.imageView2x2 };
	    winVariants.add(rdiagonal3);
	    
	    int[] row1 = { R.id.imageView0x0, R.id.imageView1x0, R.id.imageView2x0, R.id.imageView3x0, R.id.imageView4x0 };
	    winVariants.add(row1);
	    
	    int[] row2 = { R.id.imageView0x1, R.id.imageView1x1, R.id.imageView2x1, R.id.imageView3x1, R.id.imageView4x1 };
	    winVariants.add(row2);
	    
	    int[] row3 = { R.id.imageView0x2, R.id.imageView1x2, R.id.imageView2x2, R.id.imageView3x2, R.id.imageView4x2 };
	    winVariants.add(row3);
	    
	    int[] col1 = { R.id.imageView0x0, R.id.imageView0x1, R.id.imageView0x2 };
	    winVariants.add(col1);
	    
	    int[] col2 = { R.id.imageView1x0, R.id.imageView1x1, R.id.imageView1x2 };
	    winVariants.add(col2);
	    
	    int[] col3 = { R.id.imageView2x0, R.id.imageView2x1, R.id.imageView2x2 };
	    winVariants.add(col3);
	    
	    int[] col4 = { R.id.imageView3x0, R.id.imageView3x1, R.id.imageView3x2 };
	    winVariants.add(col4);
	    
	    int[] col5 = { R.id.imageView4x0, R.id.imageView4x1, R.id.imageView4x2 };
	    winVariants.add(col5);
	}

	private int isWinsGame() {
		for(int ii = 0; ii < winVariants.size(); ++ii){
			if(checkWins(winVariants.get(ii))) {
				winVariant = ii;
				return 1;
			}
		}

		for(int  id : buttonsId) {
			if(buttonsState.get(id) == ButtonState.Empty){
				return 0;
			}
		}
		
		// full house
		return 2;
	}
	
	private boolean checkWins(int[] ids) {
		int res = 0x11;
		for(int id : ids) {
			switch(getButtonState(id)) {
				case AI: res &= 0x01; break; 
				case Human: res &= 0x10; break;
				case Empty: return false;
				default: break;
			}
		}
		
		return res != 0;
	}

	private void initData() {
		buttonsState = new HashMap<Integer, ButtonState>();
		
		if(winVariants == null) {
			initWinVariants();
		}

		history = new ArrayList<Integer>();
		
	    if(disableAI) {
	    	humanFirst = true;
	    }
	    
	    loadData();
	}

	private void startGame() {
		buttonsState.clear();
		resetButtons();

		history.clear();
		winVariant = -1;
		allowReset = false;

		if(humanFirst) {
			Toast.makeText(this, getApplicationContext().getString(R.string.your_turn), Toast.LENGTH_SHORT).show();
		} else {
			turnAIFirst();
		}
	}
	
	private void saveData() {
		SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.app_name), 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("winsX", winsX);
		editor.putInt("winsO", winsO);
		editor.putBoolean("humanFirst", humanFirst);
		editor.putBoolean("disableAI", disableAI);
		editor.commit();
	}
	
	private void loadData() {
		SharedPreferences settings = getSharedPreferences(getApplicationContext().getString(R.string.app_name), 0);
		winsX = settings.getInt("winsX", 0);
		winsO = settings.getInt("winsO", 0);
		humanFirst = settings.getBoolean("humanFirst", false);
		disableAI = settings.getBoolean("disableAI", false);
	}
	
	private int getResourceState(ButtonState state, boolean mark) {
		if(state == ButtonState.AI) {
				return mark ? R.drawable.mark_xs : R.drawable.mark_x;
		}else
		if(state == ButtonState.Human) {
				return mark ? R.drawable.mark_os : R.drawable.mark_o;
		}
		return R.drawable.mark_null;
	}

	private ButtonState getButtonState(int id) {
		return buttonsState.get(id);
	}
	
	private int getRand(int max) {
		return (int) (Math.random() * (max + 1));
	}
	
	private int getButtonX(int id) {
		switch(id) {
			case R.id.imageView0x0: case R.id.imageView0x1: case R.id.imageView0x2: return 0;
			case R.id.imageView1x0: case R.id.imageView1x1: case R.id.imageView1x2: return 1;
			case R.id.imageView2x0: case R.id.imageView2x1: case R.id.imageView2x2: return 2;
			case R.id.imageView3x0: case R.id.imageView3x1: case R.id.imageView3x2: return 3;
			case R.id.imageView4x0: case R.id.imageView4x1: case R.id.imageView4x2: return 4;
			default: break;
		}
		
		return -1;
	}

	private ButtonState getButtonState(int posx, int posy) {
		return isIndexValid(posx, posy) ? buttonsState.get(buttonsId[getIndex(posx, posy)]) : ButtonState.Empty;
	}

	private void setButtonState(int id, ButtonState state) {
		buttonsState.put(id, state);
	}
	
	private boolean isIndexValid(int posx, int posy) {
		return posx >= 0 && posx < 5 && posy >= 0 && posy < 3;
	}

	private void turnHuman(int id) {
		boolean bluePlayer = humanFirst ? (history.size() % 2) == 0 : (history.size() % 2) == 1;
		
		if(disableAI){
			bluePlayer = (history.size() % 2) == 1;
		}

		markButton(id, bluePlayer ? ButtonState.Human : ButtonState.AI);
	    int ret = isWinsGame();

	    if(0 < ret) {
	        setWinsGame(ret == 2, !bluePlayer, true);
	    }
	    else
	    if(!disableAI) {
	    	turnAI();
	    }
	}

	private void turnAIFirst() {
		int id = R.id.imageView2x1;

		switch(getRand(2)) {
			case 1: id = R.id.imageView1x1; break;
			case 2: id = R.id.imageView3x1; break;
			default: break;
		}
		
		markButton(id, ButtonState.AI);
	}
	
	private void turnAI() {
		int turn = history.size() / 2 + 1;
		int res = 0;
		
		if(turn == 1) {
			if(getButtonState(1, 1) == ButtonState.Human) {
				res = (getRand(1) == 1 ? R.id.imageView0x0 : R.id.imageView0x2);
			} else
			if(getButtonState(3, 1) == ButtonState.Human) {
				res = (getRand(1) == 1 ? R.id.imageView4x0 : R.id.imageView4x2);
			} else
			if(getButtonState(1, 1) == ButtonState.Empty) {
				res = R.id.imageView1x1;
			}else
			if(getButtonState(3, 1) == ButtonState.Empty) {
				res = R.id.imageView3x1;
			}
		}

		// check possible AI wins
		if(res == 0) {
			for(int ii = 0; ii < winVariants.size(); ++ii){
				res = possibleWins(winVariants.get(ii), ButtonState.AI);
				if(res != 0) {
					break;
				}
			}
		}
		
		// block possible human wins
		if(res == 0) {
			for(int[] buttons : winVariants) {
				res = possibleWins(buttons, ButtonState.Human);
				if(res != 0) {
					break;
				}
			}
		}

		if(res == 0 && turn == 2) {
			int lastId = history.get(history.size() - 1);
			
			if(getButtonState(1, 1) == ButtonState.AI) {
				if(getButtonX(lastId) > 2 || lastId == R.id.imageView3x0 || lastId == R.id.imageView1x2) {
					res = getRand(1) == 1 ? R.id.imageView2x0 : R.id.imageView2x2;
				} else
				if(lastId == R.id.imageView2x1 || lastId == R.id.imageView2x0 || lastId == R.id.imageView2x2) {
					res = R.id.imageView3x1;
				} else
				if(lastId == R.id.imageView0x1) {
					res = getRand(1) == 1 ? R.id.imageView2x1 : R.id.imageView3x1;
				} else
				if(lastId == R.id.imageView0x0 || lastId == R.id.imageView0x2) {
					res = R.id.imageView2x1;
				}
			}else
			if(getButtonState(3, 1) == ButtonState.AI) {
				if(getButtonX(lastId) < 2 || lastId == R.id.imageView3x0 || lastId == R.id.imageView3x2) {
					res = getRand(1) == 1 ? R.id.imageView2x0 : R.id.imageView2x2;
				} else
				if(lastId == R.id.imageView2x1 || lastId == R.id.imageView2x0 || lastId == R.id.imageView2x2) {
					res = R.id.imageView1x1;
				} else
				if(lastId == R.id.imageView4x1) {
					res = getRand(1) == 1 ? R.id.imageView2x1 : R.id.imageView1x1;
				} else
				if(lastId == R.id.imageView4x0 || lastId == R.id.imageView4x2) {
					res = R.id.imageView2x1;
				}				
			}
		} else
		if(res == 0 && turn == 3) {
			if(getButtonState(2, 0) == ButtonState.Human && getButtonState(2, 2) == ButtonState.Human) {
				res = R.id.imageView2x1;
			}
		}
		
		// find two turn AI wins
		if(res == 0) {
			List<Integer> variants = new ArrayList<Integer>();

			for(int ii = 0; ii < winVariants.size(); ++ii){
				variants.addAll(twoFreeTurnVariants(winVariants.get(ii)));
			}
			
			Collections.sort(variants);
			//debugConsole(variants.toString());
			Set<Integer> setVariants = new HashSet<Integer>(variants);

			for(Integer id : setVariants) {
				int first = variants.indexOf(id);
				int last = variants.lastIndexOf(id);
				
				if(first < last) {
					res = variants.get(first);
					break;
				}
			}
		}
		
		// get random cell
		if(res == 0) {
			List<Integer> buttons = new ArrayList<Integer>();
			
			for(int id : buttonsId) {
				if(getButtonState(id) == ButtonState.Empty) {
					buttons.add(id);
				}
			}
			
			if(buttons.size() > 0) {
				res = buttons.get(getRand(buttons.size() - 1));
			}
		}

		// turn
		if(res == 0) {
			Toast.makeText(this, "Error: AI unknown turn", Toast.LENGTH_SHORT).show();
		} else {
			if(getButtonState(res) != ButtonState.Empty) {
				Toast.makeText(this, "Error: AI broken turn", Toast.LENGTH_SHORT).show();
			} else {
				boolean redPlayer = true;

				markButton(res, ButtonState.AI);
			    int ret = isWinsGame();

			    if(0 < ret) {
			        setWinsGame(ret == 2, redPlayer, false);
			    }
			}
		}	
	}

	private List<Integer> twoFreeTurnVariants(int[] buttons) {
		int count = 0;
		int free = 0;
		List<Integer> res = new ArrayList<Integer>();
		
		for(int id : buttons) {
			ButtonState state = getButtonState(id);
			
			if(state == ButtonState.AI) {
				count += 1;
			} else
			if(state == ButtonState.Empty) {
				res.add(id);
				free += 1;
			}
		}

		if(free != 2 || count + free != buttons.length) {
			res.clear();
		}

		return res;
	}

	private int possibleWins(int[] buttons, ButtonState state) {
		int count = 0;
		int res = 0;
		
		for(int id : buttons) {
			ButtonState st = getButtonState(id);
			if(state == st) {
				count += 1;
			} else
			if(st == ButtonState.Empty) {
				res = id;
			}
		}

		return count + 1 == buttons.length ? res : 0;
	}

	private void setWinsGame(boolean drawn, boolean red, boolean human) {	
		if(drawn) {
			// DRAWN GAME
			Toast.makeText(this, getApplicationContext().getString(R.string.drawn_game), Toast.LENGTH_SHORT).show();
		} else {
			if(red) {
				winsX += 1;
			} else {
				winsO += 1;
			}

			updateScore();
			
			if(! disableAI){
				if(human) {
					// Your WINS!
					Toast.makeText(this, getApplicationContext().getString(R.string.your_wins), Toast.LENGTH_SHORT).show();
				} else {
					// Your LOSS!
					Toast.makeText(this, getApplicationContext().getString(R.string.your_loss), Toast.LENGTH_SHORT).show();
				}
			} else {
				if(red) {
					// Red WINS!
					Toast.makeText(this, getApplicationContext().getString(R.string.red_wins), Toast.LENGTH_SHORT).show();
				} else {
					// Blue WINS!
					Toast.makeText(this, getApplicationContext().getString(R.string.blue_wins), Toast.LENGTH_SHORT).show();
				}
			}
		}

		enableButtons(true);
		
		if(! drawn && winVariant >= 0) {
			highlightsWinsButtons();
		}
		
		allowReset = true;
		//Toast.makeText(this, getApplicationContext().getString(R.string.push_game), Toast.LENGTH_SHORT).show();
	}

	private void highlightsWinsButtons() {
		boolean markX = getButtonState(history.get(history.size() - 1)) == ButtonState.AI;
//		Animation anim = markX ? AnimationUtils.loadAnimation(this, R.anim.x_rotate) : AnimationUtils.loadAnimation(this, R.anim.o_scale);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.x_rotate);
		int[] buttons = winVariants.get(winVariant);

		for(int id : buttons) {
			ImageView image = (ImageView) findViewById(id);
			if(image != null) {
				image.setImageResource(getResourceState(markX ? ButtonState.AI : ButtonState.Human, true));
				image.startAnimation(anim);
			}
		}
	}

	private void updateScore() {
		TextView playerX = (TextView) findViewById(R.id.textViewRed);
		if(playerX != null) {
			playerX.setText(String.valueOf(winsX));
		}
		
		TextView playerO = (TextView) findViewById(R.id.textViewBlue);
		if(playerO != null) {
			playerO.setText(String.valueOf(winsO));			
		}
	}

	private void debugConsole(String str) {
		Log.i(getApplicationContext().getString(R.string.app_name), str);		
	}
	
	private void markButton(int id, ButtonState state) {
		ImageView image = (ImageView) findViewById(id);
		
		if(image != null) {
			image.setBackgroundResource(R.drawable.mark_null);
			image.setImageResource(getResourceState(state, false));
			
			setButtonState(id, state);
			image.setEnabled(false);
			history.add(id);
		} else {
			debugConsole("unknown button: " + String.valueOf(id));
		}
	}

	public void myClickLayout(View view) {
		if(allowReset) {
			if(humanFirst) {
				humanFirst = false;
			} else {
				humanFirst = true;
			}
			startGame();
		}
	}

	public void myClickButtons(View view) {
		if(allowReset) {
			myClickLayout(view);
		} else {
			if(vibra != null) vibra.vibrate(60);
			turnHuman(view.getId());
		}
	}
}
