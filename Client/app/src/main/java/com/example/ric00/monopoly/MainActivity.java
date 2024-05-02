package com.example.ric00.monopoly;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
	String pswHash = CHANGE_ME;
	String serverUrl = CHANGE_ME;

	private static final String PREFS_NAME = "prefs";
	private static final String PREF_DARK_THEME = "dark_theme";
	static final Integer CAMERA = 0x1;
	Boolean showLogBox = false;
	Boolean defaultLog = false;
	Boolean defaultTime = false;
	Boolean isAdmin = false;
	Boolean started = false;
	Boolean verifystarted = false;
	Boolean useDarkTheme;
	JsonObjectRequest getRequest;
	List<ArrayList<String>> texts = new ArrayList<ArrayList<String>>();
	Map<String, String> ret;
	RequestQueue queue;
	Timer timer;
	Timer verifytimer;
	TimerTask timerTask;
	TimerTask verifytimerTask;
	String HTTPparams = "";
	String qrUser = "";
	String user;
	Map<String, String> write = new HashMap<>();
	private NfcAdapter mNfcAdapter;

	//Admin menu
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.admin, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.adminize:
			{
				final AlertDialog.Builder PswInput = new AlertDialog.Builder(MainActivity.this);
				final EditText PswIn = new EditText(MainActivity.this);
				PswIn.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				PswInput.setView(PswIn);
				PswInput.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
					}
				});
				PswInput.setNeutralButton("Annulla", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
					}
				});
				final AlertDialog PswInputEdit = PswInput.create();
				PswInputEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				PswInputEdit.show();
				PswInputEdit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						final String psw = PswIn.getText().toString();
						PswInputEdit.dismiss();
						new Thread(new Runnable()
						{
							public void run()
							{
								if (BCrypt.checkpw(psw, pswHash))
								{
									isAdmin = true;
									verifyLoop(true);
									runOnUiThread(new Runnable()
									{
										@Override
										public void run()
										{
											((Button) findViewById(R.id.registerButton)).setVisibility(View.VISIBLE);
											((Button) findViewById(R.id.bankruptButton)).setVisibility(View.VISIBLE);
											findViewById(R.id.requestPaymentButton).setVisibility(View.VISIBLE);
											((Button) findViewById(R.id.requestPaymentButton)).setText("Transazione");
										}
									});
								}
								else
								{
									isAdmin = false;
									verifyLoop(false);
									runOnUiThread(new Runnable()
									{
										@Override
										public void run()
										{
											((Button) findViewById(R.id.registerButton)).setVisibility(View.GONE);
											((Button) findViewById(R.id.bankruptButton)).setVisibility(View.GONE);
											findViewById(R.id.requestPaymentButton).setVisibility(View.GONE);//
											/*
											((Button) findViewById(R.id.requestPaymentButton)).setText("Richiedi pagamento");
											if(mNfcAdapter != null)
											{
												findViewById(R.id.requestPaymentButton).setVisibility(View.GONE);
											}
											else
											{
												findViewById(R.id.requestPaymentButton).setVisibility(View.VISIBLE);
											}
											*/
										}
									});
								}
							}
						}).start();
					}
				});

				break;
			}
		}
		return true;
	}

	//Stop all timers
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		try
		{
			timer.cancel();
			timer.purge();
			verifytimer.cancel();
			verifytimer.purge();
		} catch (NullPointerException e)
		{
		}
	}

	//NFC check
	@Override
	protected void onResume()
	{
		super.onResume();
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter != null)
		{
			mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
			if (mNfcAdapter.isEnabled())
			{
				//logger("NFC is enabled.");
			}
			else
			{
				logger("NFC is disabled.");
				AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
				alertbox.setTitle("Errore");
				alertbox.setMessage("Accendi l'NFC per giocare");
				alertbox.setPositiveButton("Apri impostazioni", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
						{
							Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
							startActivity(intent);
						}
						else
						{
							Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
							startActivity(intent);
						}
					}
				});
				/*
				alertbox.setOnDismissListener(new DialogInterface.OnDismissListener()
				{
					@Override
					public void onDismiss(final DialogInterface arg0)
					{
						logger("Chiuso");
					}
				});
				*/
				alertbox.setCancelable(false);
				alertbox.show();
			}
		}
		else
		{
			AlertDialog noNFC = new AlertDialog.Builder(this).create();
			noNFC.setTitle("NFC mancante");
			noNFC.setMessage("È possibile usare solamente il QR");
			//noNFC.setMessage("Per richiedere pagamenti usa il bottone che appare");
			noNFC.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					//findViewById(R.id.requestPaymentButton).setVisibility(View.VISIBLE);
				}
			});
			noNFC.show();
		}
	}

	//NFC R/W
	@Override
	protected void onNewIntent(Intent intent)
	{
		Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tag != null)
		{
			//Toast.makeText(this, "NFCiccio", Toast.LENGTH_SHORT).show();
			Ndef ndef = Ndef.get(tag);
			if (write.isEmpty())
			{
				try
				{
					ndef.connect();
					NdefMessage ndefMessage = ndef.getNdefMessage();
					if (ndefMessage != null)
					{
						String read = new String(ndefMessage.getRecords()[0].getPayload());
						//logger("Read: " + read);
						Map<String, String> params = new HashMap<>();
						params.put("cardId", read);
						getData(new VolleyCallback()
						{
							@Override
							public void onSuccess(JSONObject out, Boolean success)
							{
								if (success)
								{
									final String cardUser = JsonToString(out).get("user");
									if (!cardUser.equals(user))
									{
										//Request payment
										final AlertDialog.Builder PayInput = new AlertDialog.Builder(MainActivity.this);
										PayInput.setTitle("Preleva da " + cardUser);
										final EditText PayIn = new EditText(MainActivity.this);
										PayIn.setInputType(InputType.TYPE_CLASS_NUMBER);
										Drawable monodollar = getDrawable(R.drawable.monodollar);
										monodollar.setColorFilter(useDarkTheme ? ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark) : ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light), PorterDuff.Mode.MULTIPLY);
										monodollar.setBounds(0, 0, (int) PayIn.getTextSize(), (int) PayIn.getTextSize());
										PayIn.setCompoundDrawables(null, null, monodollar, null);
										PayInput.setView(PayIn);
										PayInput.setPositiveButton("OK", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which)
											{
											}
										});
										PayInput.setNeutralButton("Annulla", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which)
											{
											}
										});
										final AlertDialog PayInputEdit = PayInput.create();
										PayInputEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
										PayInputEdit.show();
										PayInputEdit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												String value = PayIn.getText().toString();
												if (!value.isEmpty())
												{
													Map<String, String> payparams = new HashMap<>();
													payparams.put("from", cardUser);
													payparams.put("to", user);
													payparams.put("value", value);
													payparams.put("verified", "1");
													getData(new VolleyCallback()
													{
														@Override
														public void onSuccess(JSONObject out, Boolean success)
														{
															if (success)
															{
																PayInputEdit.dismiss();
															}
														}
													}, "setPayment", payparams);
												}
											}
										});
									}
									else
									{
										Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
										try
										{
											intent.setPackage("com.google.android.youtube");
											MainActivity.this.startActivity(intent);
										} catch (ActivityNotFoundException e)
										{
											intent.setPackage(null);
											MainActivity.this.startActivity(intent);
										}
									}
								}
							}
						}, "getUserFromCardId", params);
					}
					else
					{
						AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
						alertDialog.setTitle("Errore");
						alertDialog.setMessage("Carta non riconosciuta");
						alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
							}
						});
						alertDialog.show();
						logger("Null tag");
					}
					ndef.close();

				} catch (IOException | FormatException e)
				{
					AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
					alertDialog.setTitle("Errore");
					alertDialog.setMessage("Errore di lettura, tenere la carta a contatto più a lungo");
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog, int which)
						{
						}
					});
					alertDialog.show();
					logger(e.toString());
				}
			}
			else
			{
				AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
					}
				});
				String cardId = write.get("cardId");
				String newUser = write.get("user");
				try
				{
					ndef.connect();
					NdefRecord mimeRecord = NdefRecord.createMime("text/plain", cardId.getBytes(Charset.forName("US-ASCII")));
					ndef.writeNdefMessage(new NdefMessage(mimeRecord));
					ndef.close();
					//logger("Written: " + write);
					alertDialog.setTitle("Completato");
					alertDialog.setMessage("Carta di " + newUser + " registrata");
				} catch (IOException | FormatException e)
				{
					e.printStackTrace();
					logger("Write error: " + e.toString());
					alertDialog.setTitle("Errore");
					alertDialog.setMessage("Errore sconosciuto nella registrazione della carta");
				}
				alertDialog.show();
				write.clear();
			}
		}
	}

	public void onCreate(Bundle savedInstanceState)
	{
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);
		user = preferences.getString("user", "");
		if (useDarkTheme)
		{
			setTheme(R.style.AppThemeDark);
		}
		else
		{
			setTheme(R.style.AppThemeLight);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Bitmap Icon = drawableToBitmap(getDrawable(R.drawable.monodollar));
		ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription(getTitle().toString(), Icon, useDarkTheme ? ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryLight) : ContextCompat.getColor(MainActivity.this, R.color.colorPrimary));
		setTaskDescription(tDesc);
		final TextView textLog = (TextView) findViewById(R.id.textLog);
		textLog.setGravity(Gravity.BOTTOM);
		textLog.setMovementMethod(new ScrollingMovementMethod());

		//Show/Hide log
		if (showLogBox)
		{
			textLog.setVisibility(View.VISIBLE);
		}
		else
		{
			textLog.setVisibility(View.INVISIBLE);
		}

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			requestPermissions(new String[]{Manifest.permission.NFC}, CAMERA);
			requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA);
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.NFC}, CAMERA);
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA);
		}
		//Bankrupt user
		final Button bankruptButton = findViewById(R.id.bankruptButton);
		bankruptButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Map<String, String> params = new HashMap<>();
				getData(new VolleyCallback()
				{
					@Override
					public void onSuccess(JSONObject result, Boolean success)
					{
						if (success)
						{
							Map<String, String> users = JsonToString(result);
							final AlertDialog.Builder BankruptInput = new AlertDialog.Builder(MainActivity.this);
							BankruptInput.setTitle("Utente da mandare in bancarotta");
							LinearLayout layout = new LinearLayout(MainActivity.this);
							layout.setOrientation(LinearLayout.VERTICAL);
							final Spinner BankruptInputDD = new Spinner(MainActivity.this);
							final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item)
							{
								@Override
								public boolean isEnabled(int position)
								{
									if (position == 0)
									{
										return false;
									}
									else
									{
										return true;
									}
								}

								@Override
								public View getDropDownView(int position, View convertView, ViewGroup parent)
								{
									View view = super.getDropDownView(position, convertView, parent);
									TextView tv = (TextView) view;
									if (position == 0)
									{
										tv.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.materialGrey));
									}
									else
									{
										tv.setTextColor(useDarkTheme ? ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark) : ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light));
									}
									tv.setPadding(dptopx(10), dptopx(10), dptopx(10), dptopx(10));
									return view;
								}
							};
							adapter.add("Seleziona un utente");
							for (Map.Entry<String, String> entry : users.entrySet())
							{
								adapter.add(entry.getValue());
							}
							BankruptInputDD.setAdapter(adapter);
							BankruptInputDD.setPadding(0, 0, 0, 0);
							layout.setPadding(dptopx(24), dptopx(24), dptopx(24), 0);
							layout.addView(BankruptInputDD);
							BankruptInput.setView(layout);
							BankruptInput.setPositiveButton("OK", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
								}
							});
							BankruptInput.setNeutralButton("Annulla", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
								}
							});
							final AlertDialog BankruptInputEdit = BankruptInput.create();
							BankruptInputEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
							BankruptInputEdit.show();
							BankruptInputEdit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									final String killuser = BankruptInputDD.getSelectedItem().toString();
									Integer pos = BankruptInputDD.getSelectedItemPosition();
									if (pos > 0)
									{
										AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(MainActivity.this);
										confirmBuilder.setTitle("Conferma");
										confirmBuilder.setMessage("Sei proprio sicuro che vuoi mandare in bancarotta " + killuser + "?");
										confirmBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which)
											{
												Map<String, String> params = new HashMap<>();
												params.put("user", killuser);
												getData(new VolleyCallback()
												{
													@Override
													public void onSuccess(JSONObject result, Boolean success)
													{
														if (success)
														{
															BankruptInputEdit.dismiss();
														}
													}
												}, "deleteUser", params);
											}
										});
										confirmBuilder.setNeutralButton("Annulla", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which)
											{
											}
										});
										AlertDialog confirm = confirmBuilder.create();
										confirm.show();
									}
								}
							});
						}
					}
				}, "getUsers", params);
			}
		});

		//Scan QR
		final Button scanQR = findViewById(R.id.scanQR);
		scanQR.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startScanQR();
			}
		});

		//Register
		write.clear();
		final Button registerButton = findViewById(R.id.registerButton);
		registerButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				PopupMenu menu = new PopupMenu(MainActivity.this, registerButton);
				final String opt1 = "Registra carta";
				final String opt2 = "Registra QR";
				menu.getMenu().add(opt1);
				menu.getMenu().add(opt2);
				menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
				{
					@Override
					public boolean onMenuItemClick(MenuItem item)
					{
						String action = item.getTitle().toString();
						//Register card
						if (action.equals(opt1))
						{
							Map<String, String> params = new HashMap<>();
							getData(new VolleyCallback()
							{
								@Override
								public void onSuccess(JSONObject result, Boolean success)
								{
									if (success)
									{
										Map<String, String> users = JsonToString(result);
										final AlertDialog.Builder NFCInput = new AlertDialog.Builder(MainActivity.this);
										NFCInput.setTitle("Nome utente del proprietario");
										LinearLayout layout = new LinearLayout(MainActivity.this);
										layout.setOrientation(LinearLayout.VERTICAL);
										final Spinner NFCInputDD = new Spinner(MainActivity.this);
										final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item)
										{
											@Override
											public boolean isEnabled(int position)
											{
												if (position == 0)
												{
													return false;
												}
												else
												{
													return true;
												}
											}

											@Override
											public View getDropDownView(int position, View convertView, ViewGroup parent)
											{
												View view = super.getDropDownView(position, convertView, parent);
												TextView tv = (TextView) view;
												if (position == 0)
												{
													tv.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.materialGrey));
												}
												else
												{
													tv.setTextColor(useDarkTheme ? ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark) : ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light));
												}
												tv.setPadding(dptopx(10), dptopx(10), dptopx(10), dptopx(10));
												return view;
											}
										};
										adapter.add("Seleziona un utente");
										for (Map.Entry<String, String> entry : users.entrySet())
										{
											adapter.add(entry.getValue());
										}
										NFCInputDD.setAdapter(adapter);
										NFCInputDD.setPadding(0, 0, 0, 0);
										layout.setPadding(dptopx(24), dptopx(24), dptopx(24), 0);
										layout.addView(NFCInputDD);
										NFCInput.setView(layout);
										NFCInput.setPositiveButton("OK", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which)
											{
											}
										});
										NFCInput.setNeutralButton("Annulla", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which)
											{
											}
										});
										final AlertDialog NFCInputEdit = NFCInput.create();
										NFCInputEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
										NFCInputEdit.show();
										NFCInputEdit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												String newuser = NFCInputDD.getSelectedItem().toString();
												Integer pos = NFCInputDD.getSelectedItemPosition();
												if (pos > 0)
												{
													Map<String, String> params = new HashMap<>();
													params.put("user", newuser);
													getData(new VolleyCallback()
													{
														@Override
														public void onSuccess(JSONObject result, Boolean success)
														{
															if (success)
															{
																NFCInputEdit.dismiss();
																write = JsonToString(result);
															}
															//logger("Done", true);
														}
													}, "getCardIdForUser", params);
												}
											}
										});
									}
								}
							}, "getUsers", params);
						}
						//Register QR
						else if (action.equals(opt2))
						{
							Map<String, String> params = new HashMap<>();
							getData(new VolleyCallback()
							{
								@Override
								public void onSuccess(JSONObject result, Boolean success)
								{
									if (success)
									{
										Map<String, String> users = JsonToString(result);
										final AlertDialog.Builder NFCInput = new AlertDialog.Builder(MainActivity.this);
										NFCInput.setTitle("Nome utente del proprietario");
										LinearLayout layout = new LinearLayout(MainActivity.this);
										layout.setOrientation(LinearLayout.VERTICAL);
										final Spinner NFCInputDD = new Spinner(MainActivity.this);
										final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item)
										{
											@Override
											public boolean isEnabled(int position)
											{
												if (position == 0)
												{
													return false;
												}
												else
												{
													return true;
												}
											}

											@Override
											public View getDropDownView(int position, View convertView, ViewGroup parent)
											{
												View view = super.getDropDownView(position, convertView, parent);
												TextView tv = (TextView) view;
												if (position == 0)
												{
													tv.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.materialGrey));
												}
												else
												{
													tv.setTextColor(useDarkTheme ? ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark) : ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light));
												}
												tv.setPadding(dptopx(10), dptopx(10), dptopx(10), dptopx(10));
												return view;
											}
										};
										adapter.add("Seleziona un utente");
										for (Map.Entry<String, String> entry : users.entrySet())
										{
											adapter.add(entry.getValue());
										}
										NFCInputDD.setAdapter(adapter);
										NFCInputDD.setPadding(0, 0, 0, 0);
										layout.setPadding(dptopx(24), dptopx(24), dptopx(24), 0);
										layout.addView(NFCInputDD);
										NFCInput.setView(layout);
										NFCInput.setPositiveButton("OK", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which)
											{
											}
										});
										NFCInput.setNeutralButton("Annulla", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which)
											{
											}
										});
										final AlertDialog NFCInputEdit = NFCInput.create();
										NFCInputEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
										NFCInputEdit.show();
										NFCInputEdit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												String newuser = NFCInputDD.getSelectedItem().toString();
												Integer pos = NFCInputDD.getSelectedItemPosition();
												if (pos > 0)
												{
													qrUser = newuser;
													NFCInputEdit.dismiss();
													startScanQR();
												}
											}
										});
									}
								}
							}, "getUsers", params);
						}
						return true;
					}
				});
				menu.show();
			}
		});

		//Ask username
		final AlertDialog.Builder userDialog = new AlertDialog.Builder(this);
		userDialog.setTitle("Imposta un nome");
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		userDialog.setView(input);
		userDialog.setPositiveButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
			}
		});
		userDialog.setNeutralButton("Annulla", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
			}
		});
		userDialog.setCancelable(false);
		final AlertDialog userDialogEdit = userDialog.create();
		if (!user.isEmpty())
		{
			setUser(user);
			userDialogEdit.setCancelable(true);
		}
		else
		{
			userDialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			userDialogEdit.show();
			final Button cancelButton = userDialogEdit.getButton(AlertDialog.BUTTON_NEUTRAL);
			cancelButton.setEnabled(false);
			userDialogEdit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					String user = "";
					user = input.getText().toString();
					if (!user.isEmpty())
					{
						setUser(user);
						cancelButton.setEnabled(true);
						userDialogEdit.setCancelable(true);
						userDialogEdit.dismiss();
						Map<String, String> params = new HashMap<>();
					}
				}
			});
		}

		//Change username
		final Button setUserButton = findViewById(R.id.setUserButton);
		setUserButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				userDialogEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				userDialogEdit.show();
				userDialogEdit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						String user = "";
						user = input.getText().toString();
						if (!user.isEmpty())
						{
							setUser(user);
							userDialogEdit.setCancelable(true);
							userDialogEdit.dismiss();
						}
					}
				});

			}
		});

		//findViewById(R.id.requestPaymentButton).setVisibility(View.VISIBLE);
		//Request payment
		final Button requestPaymentButton = findViewById(R.id.requestPaymentButton);
		requestPaymentButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				Map<String, String> params = new HashMap<>();
				if (isAdmin)
				{
					params.put("isAdmin", "1");
				}
				else
				{
					params.put("user", user);
				}
				getData(new VolleyCallback()
				{
					@Override
					public void onSuccess(JSONObject result, Boolean success)
					{
						if (success)
						{
							Map<String, String> users = JsonToString(result);
							final AlertDialog.Builder PayInput = new AlertDialog.Builder(MainActivity.this);
							if (isAdmin)
							{
								PayInput.setTitle("Transazione");
							}
							else
							{
								PayInput.setTitle("Richiedi il pagamento da un utente");
							}
							LinearLayout layout = new LinearLayout(MainActivity.this);
							layout.setOrientation(LinearLayout.VERTICAL);
							final Spinner PayUserDD = new Spinner(MainActivity.this);
							final Spinner PayUserDD2 = new Spinner(MainActivity.this);
							final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item)
							{
								@Override
								public boolean isEnabled(int position)
								{
									if (position == 0)
									{
										return false;
									}
									else
									{
										return true;
									}
								}

								@Override
								public View getDropDownView(int position, View convertView, ViewGroup parent)
								{
									View view = super.getDropDownView(position, convertView, parent);
									TextView tv = (TextView) view;
									if (position == 0)
									{
										tv.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.materialGrey));
									}
									else
									{
										tv.setTextColor(useDarkTheme ? ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark) : ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light));
									}
									tv.setPadding(dptopx(10), dptopx(10), dptopx(10), dptopx(10));
									return view;
								}
							};
							if (isAdmin)
							{
								adapter.add("Da");
							}
							else
							{
								adapter.add("Seleziona un utente");
							}
							for (Map.Entry<String, String> entry : users.entrySet())
							{
								adapter.add(entry.getValue());
							}
							PayUserDD.setAdapter(adapter);
							PayUserDD.setPadding(0, 0, 0, 0);
							layout.setPadding(dptopx(24), dptopx(24), dptopx(24), 0);
							layout.addView(PayUserDD);
							if (isAdmin)
							{
								final ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item)
								{
									@Override
									public boolean isEnabled(int position)
									{
										if (position == 0)
										{
											return false;
										}
										else
										{
											return true;
										}
									}

									@Override
									public View getDropDownView(int position, View convertView, ViewGroup parent)
									{
										View view = super.getDropDownView(position, convertView, parent);
										TextView tv = (TextView) view;
										if (position == 0)
										{
											tv.setTextColor(ContextCompat.getColor(MainActivity.this,R.color.materialGrey));
										}
										else
										{
											tv.setTextColor(useDarkTheme ? ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark) : ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light));
										}
										tv.setPadding(dptopx(10), dptopx(10), dptopx(10), dptopx(10));
										return view;
									}
								};
								adapter2.add("A");
								for (Map.Entry<String, String> entry : users.entrySet())
								{
									adapter2.add(entry.getValue());
								}
								PayUserDD2.setAdapter(adapter2);
								PayUserDD2.setPadding(0, 0, 0, 0);
								layout.addView(PayUserDD2);
							}
							final EditText PayIn = new EditText(MainActivity.this);
							PayIn.setInputType(InputType.TYPE_CLASS_NUMBER);
							Drawable monodollar = getDrawable(R.drawable.monodollar);
							monodollar.setColorFilter(useDarkTheme ? ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark) : ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light), PorterDuff.Mode.MULTIPLY);
							monodollar.setBounds(0, 0, (int) PayIn.getTextSize(), (int) PayIn.getTextSize());
							PayIn.setCompoundDrawables(null, null, monodollar, null);
							layout.addView(PayIn);
							PayInput.setView(layout);
							PayInput.setPositiveButton("OK", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
								}
							});
							PayInput.setNeutralButton("Annulla", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
								}
							});
							final AlertDialog PayInputEdit = PayInput.create();
							PayInputEdit.show();
							PayInputEdit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									String value = PayIn.getText().toString();
									String fromUser = PayUserDD.getSelectedItem().toString();
									String toUser;
									Integer pos = PayUserDD.getSelectedItemPosition();
									Integer pos2;
									if (isAdmin)
									{
										toUser = PayUserDD2.getSelectedItem().toString();
										pos2 = PayUserDD2.getSelectedItemPosition();
									}
									else
									{
										toUser = user;
										pos2 = pos + 1;
									}
									if (!value.isEmpty() && pos > 0 && pos2 > 0 && pos2 != pos)
									{
										Map<String, String> payparams = new HashMap<>();
										payparams.put("from", fromUser);
										payparams.put("to", toUser);
										payparams.put("value", value);
										payparams.put("verified", isAdmin ? "1" : "0");
										getData(new VolleyCallback()
										{
											@Override
											public void onSuccess(JSONObject out, Boolean success)
											{
												if (success)
												{
													PayInputEdit.dismiss();
													if (!isAdmin)
													{
														AlertDialog.Builder ApproveNotification = new AlertDialog.Builder(MainActivity.this);
														ApproveNotification.setTitle("Operazione completata");
														ApproveNotification.setMessage("La transazione deve essere approvata dal banchiere");
														ApproveNotification.setPositiveButton("OK", new DialogInterface.OnClickListener()
														{
															@Override
															public void onClick(DialogInterface dialog, int which)
															{
															}
														});
														AlertDialog ApproveNitificationFinal = ApproveNotification.create();
														ApproveNitificationFinal.show();
													}
												}
											}
										}, "setPayment", payparams);
									}
								}
							});
						}
					}
				}, "getUsers", params);
			}
		});
		final Button deletePreferences = findViewById(R.id.deletePreferencesButton);

		//Reset preferences
		deletePreferences.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
				editor.clear().apply();
				Intent intent = getIntent();
				finish();
				startActivity(intent);
			}
		});

		//Theme switch
		Switch toggle = (Switch) findViewById(R.id.blackTheme);
		toggle.setChecked(useDarkTheme);
		toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton view, boolean isChecked)
			{
				toggleTheme(isChecked);
			}
		});
	}

	private void toggleTheme(boolean darkTheme)
	{
		SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
		editor.putBoolean(PREF_DARK_THEME, darkTheme);
		editor.apply();
		Intent intent = getIntent();
		finish();
		startActivity(intent);
		this.overridePendingTransition(0, 0);
	}

	private void setUser(final String usr)
	{
		Map<String, String> params = new HashMap<>();
		params.put("from", user);
		params.put("to", usr);
		try
		{
			timer.cancel();
			timer.purge();
		} catch (NullPointerException e)
		{
		}
		getData(new VolleyCallback()
		{
			@Override
			public void onSuccess(JSONObject result, Boolean success)
			{
				if (success)
				{
					user = usr;
					setTitle("Monopoly - " + usr);
					SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
					editor.putString("user", usr);
					editor.apply();
				}
				try
				{
					queue.cancelAll(new RequestQueue.RequestFilter()
					{
						@Override
						public boolean apply(Request<?> request)
						{
							return true;
						}
					});
				} catch (NullPointerException e)
				{
				}
				updateLoop(user);
			}
		}, "changeUser", params);
	}

	public void updateLoop(final String user)
	{
		TimerTask timerTask = new TimerTask()
		{
			@Override
			public void run()
			{
				Map<String, String> params = new HashMap<>();
				params.put("user", user);
				getData(new VolleyCallback()
				{
					@Override
					public void onSuccess(JSONObject out, Boolean success)
					{
						if (success)
						{
							LinearLayout smallBalance = (LinearLayout) findViewById(R.id.smallBalance);
							smallBalance.removeAllViews();
							Map<String, Map<String, String>> map = JsonToStringMap(out);
							Boolean first = true;
							int id = 0;
							for (Map.Entry<String, Map<String, String>> entry : map.entrySet())
							{
								String str1 = entry.getValue().get("user");
								String str2 = entry.getValue().get("balance");
								String str3 = entry.getValue().get("change");
								if (first)
								{
									RelativeLayout userBalance = createBalanceRow(str1, str2, str3);
									userBalance.setId(R.id.userBalance);
									int index = ((LinearLayout) findViewById(R.id.bigBalance)).indexOfChild(findViewById(R.id.userBalance));
									((LinearLayout) findViewById(R.id.bigBalance)).removeView(findViewById(R.id.userBalance));
									//logger("Removed " + index + "\n",true);
									((LinearLayout) findViewById(R.id.bigBalance)).addView(userBalance, 0);
									userBalance.setBackgroundColor(((ColorDrawable) getWindow().getDecorView().getBackground()).getColor() + Integer.parseInt("101010", 16) * (useDarkTheme ? (int) 1 : (int) -1));
									first = false;
								}
								else
								{
									RelativeLayout row = createBalanceRow(str1, str2, str3);
									row.setId(id);
									int index = smallBalance.indexOfChild(findViewById(id));
									//logger("Removed " + index + "\n",true);
									smallBalance.addView(row, index);
									id++;
								}
							}
						}
					}
				}, "getBalance", params);
			}
		};
		if (started)
		{
			timer.cancel();
			timer.purge();
		}
		timer = new Timer();
		timer.scheduleAtFixedRate(timerTask, 0, 1000);
		started = true;
	}

	public void verifyLoop(Boolean startstop)
	{
		TimerTask verifytimerTask = new TimerTask()
		{
			Boolean getNext = true;

			@Override
			public void run()
			{
				if (getNext)
				{
					getData(new VolleyCallback()
					{
						@Override
						public void onSuccess(JSONObject out, Boolean success)
						{
							getNext = false;
							if (success)
							{
								final Map<String, String> map = JsonToString(out);
								AlertDialog.Builder ApproveDialog = new AlertDialog.Builder(MainActivity.this);
								ApproveDialog.setTitle("Richiesta approvazione pagamento");
								ApproveDialog.setCancelable(false);
								SpannableString ss = new SpannableString("Transazione di " + map.get("value").toString() + " da " + map.get("from") + " a " + map.get("to"));
								Drawable monodollar = getDrawable(R.drawable.monodollar);
								monodollar.setColorFilter(useDarkTheme ? ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark) : ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light), PorterDuff.Mode.MULTIPLY);
								int w = monodollar.getIntrinsicWidth();
								int h = monodollar.getIntrinsicHeight();
								monodollar.setBounds((int) Math.round(0.1 * w), (int) Math.round(0.05 * h), (int) Math.round(w), (int) Math.round(0.95 * h)); //Shift per lo 0.9
								ImageSpan span = new ImageSpan(monodollar);
								Integer len = String.valueOf("Transazione di " + map.get("value").toString()).length();
								ss.setSpan(span, len, len + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
								ApproveDialog.setMessage(ss);
								ApproveDialog.setPositiveButton("Approva", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										Map<String, String> params = new HashMap<>();
										params.put("verify", "1");
										getData(new VolleyCallback()
										{
											@Override
											public void onSuccess(JSONObject result, Boolean success)
											{
												getNext = true;
											}
										}, "setVerifiedPayment", params);
									}
								});
								ApproveDialog.setNeutralButton("Rifiuta", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										Map<String, String> params = new HashMap<>();
										params.put("verify", "0");
										getData(new VolleyCallback()
										{
											@Override
											public void onSuccess(JSONObject result, Boolean success)
											{
												getNext = true;
											}
										}, "setVerifiedPayment", params);
									}
								});
								ApproveDialog.setCancelable(false);
								AlertDialog ApproveDialogFinal = ApproveDialog.create();
								ApproveDialogFinal.show();
							}
						}
					}, "getUnverifiedPayments");
				}
			}
		};
		if (verifystarted)
		{
			verifytimer.cancel();
			verifytimer.purge();
		}
		if (startstop)
		{
			verifytimer = new Timer();
			verifytimer.scheduleAtFixedRate(verifytimerTask, 0, 1000);
			verifystarted = true;
		}
	}


	private void getData(final VolleyCallback callback, final String method)
	{
		getData(callback, method, new HashMap<String, String>(), defaultLog, defaultTime); //Madonna
	}

	private void getData(final VolleyCallback callback, final String method, Map<String, String> params)
	{
		getData(callback, method, params, defaultLog, defaultTime); //Madonna
	}

	private void getData(final VolleyCallback callback, final String method, Map<String, String> params, Boolean log)
	{
		getData(callback, method, params, log, defaultTime); //Madonna
	}

	private void getData(final VolleyCallback callback, final String method, Map<String, String> params, final Boolean log, final Boolean time)
	{
		String[] from = new String[]{"%", "#", "&", "@", "`", "/", ":", ";", "<", "=", ">", "?", "[", "\\", "]", "^", "{", "|", "}", "~", "“", "‘", "+", ","}; //Il % deve essere il primo
		String[] to = new String[]{"%25", "%23", "%26", "%40", "%60", "%2F", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F", "%5B", "%5C", "%5D", "%5E", "%7B", "%7C", "%7D", "%7E", "%22", "%27", "%2B", "%2C"};
		HTTPparams = "";
		for (Map.Entry<String, String> entry : params.entrySet())
		{
			if (!HTTPparams.isEmpty())
			{
				HTTPparams += "&";
			}
			String val = entry.getValue();
			for (int i = 0;
				 i < from.length;
				 i++)
			{
				val = val.replace(from[i], to[i]);
			}
			HTTPparams += entry.getKey() + "=" + val;
		}
		if (queue == null)
		{
			queue = Volley.newRequestQueue(this);
		}
		String url = serverUrl + "/" + method + "/?" + HTTPparams;
		getRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
		{
			@Override
			public void onResponse(JSONObject response)
			{
				if (time)
				{
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					logger("Time: " + timestamp.toString(), true);
					logger("Response", true);
				}
				/*
				logger("Result: " + String.valueOf(response.getBoolean("result")) + "\n",true);
				 */
				Boolean stop = false;
				if (response.has("lost"))
				{
					try
					{
						if (response.getBoolean("lost"))
						{
							AlertDialog.Builder serverErrorBuilder = new AlertDialog.Builder(MainActivity.this);
							serverErrorBuilder.setTitle("Hai perso!");
							serverErrorBuilder.setMessage("Non puoi fare più niente");
							serverErrorBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
								}
							});
							AlertDialog serverError = serverErrorBuilder.create();
							//serverError.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
							serverError.show();
							stop = true;
							try
							{
								timer.cancel();
								timer.purge();
								verifytimer.cancel();
								verifytimer.purge();
							} catch (NullPointerException e)
							{
							}
						}
					} catch (JSONException e)
					{
						logger("Errore nell'errore");
					}
				}
				else if (response.has("error"))
				{
					try
					{
						if (response.getBoolean("error"))
						{
							AlertDialog.Builder serverErrorBuilder = new AlertDialog.Builder(MainActivity.this);
							serverErrorBuilder.setTitle("Errore");
							serverErrorBuilder.setMessage("L'ultima richiesta al server ha prodotto un errore" + (response.has("description") ? ": " + response.getString("description") : " sconosciuto"));
							serverErrorBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
								}
							});
							AlertDialog serverError = serverErrorBuilder.create();
							serverError.show();
							stop = true;
						}
					} catch (JSONException e)
					{
						logger("Errore nell'errore");
					}
				}
				Iterator<String> keys = response.keys();
				while (keys.hasNext())
				{
					try
					{
						String key = keys.next();
						String str = response.getString(key);
						if (log)
						{
							logger(key.toString() + ": " + str, true);
						}
					} catch (JSONException e)
					{
						if (log)
						{
							logger("getData Json error: " + e.toString());
						}
					}
				}
				callback.onSuccess(response, !stop);
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (log)
				{
					logger("getData response error: " + error.toString());
				}
			}
		});
		queue.add(getRequest);
	}

	public Map<String, String> JsonToString(JSONObject response)
	{
		Map<String, String> ret = new LinkedHashMap<>();
		Iterator<String> keys = response.keys();
		while (keys.hasNext())
		{
			try
			{
				String key = keys.next();
				String str = response.getString(key);
				ret.put(key.toString(), str);
			} catch (JSONException e)
			{
				if (showLogBox)
				{
					logger("Error in JsonToString: " + e.toString());
				}
			}
		}
		return ret;
	}

	public Map<String, Map<String, String>> JsonToStringMap(JSONObject response)
	{
		Map<String, Map<String, String>> ret = new LinkedHashMap<>();
		Iterator<String> keys = response.keys();
		while (keys.hasNext())
		{
			try
			{
				String key = keys.next();
				JSONObject obj = response.getJSONObject(key);
				ret.put(key.toString(), JsonToString(obj));
			} catch (JSONException e)
			{
				Log.e("Error in JsonToString", e.toString());
			}
		}
		return ret;
	}

	public int dptopx(float dp)
	{
		float density = getResources().getDisplayMetrics().density;
		return Math.round((float) dp * density);
	}

	public float pxtodp(int px)
	{
		float density = getResources().getDisplayMetrics().density;
		return Math.round((float) px / density);
	}

	public RelativeLayout createBalanceRow(String str1, String str2, String str3)
	{
		//Log.i("Tag", str1 + ":" + str2 + ":" + str3);
		int lstr1 = str1.length();
		int lstr2 = str2.length() + 1;
		int lstr3 = str3.length() + 1;
		SpannableString sstr1 = new SpannableString(str1);
		SpannableString sstr2 = new SpannableString(str2 + " ");
		SpannableString sstr3 = new SpannableString(str3 + " ");
		int color1;
		if (useDarkTheme)
		{
			color1 = ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark);
		}
		else
		{
			color1 = ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light);
		}
		int color2 = color1;
		int color3;
		try
		{
			int istr3 = Integer.parseInt(str3);
			if (istr3 > 0)
			{
				color3 = ContextCompat.getColor(MainActivity.this,R.color.materialGreen);
			}
			else if (istr3 == 0)
			{
				color3 = ContextCompat.getColor(MainActivity.this,R.color.materialGrey);
			}
			else
			{
				color3 = ContextCompat.getColor(MainActivity.this,R.color.materialRed);
			}
		} catch (NumberFormatException e)
		{
			color3 = ContextCompat.getColor(MainActivity.this,R.color.materialGrey);
		}
		sstr1.setSpan(new ForegroundColorSpan(color1), 0, lstr1, 0);
		sstr2.setSpan(new ForegroundColorSpan(color2), 0, lstr2, 0);
		sstr2.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, lstr2, 0);
		sstr3.setSpan(new ForegroundColorSpan(color3), 0, lstr3, 0);
		sstr2.setSpan(new RelativeSizeSpan(1.5f), 0, lstr2, 0);
		RelativeLayout rl1 = new RelativeLayout(getApplicationContext());
		RelativeLayout rl2 = new RelativeLayout(getApplicationContext());
		TextView text1 = new TextView(getApplicationContext());
		TextView text2 = new TextView(getApplicationContext());
		TextView text3 = new TextView(getApplicationContext());
		Drawable monodollar2 = getDrawable(R.drawable.monodollar);
		monodollar2.setColorFilter(color2, PorterDuff.Mode.MULTIPLY);
		monodollar2.setBounds(0, 0, (int) (text2.getTextSize() * 1.5), (int) (text2.getTextSize() * 1.5));
		text2.setCompoundDrawables(null, null, monodollar2, null);
		Drawable monodollar3 = monodollar2.getConstantState().newDrawable().mutate();
		monodollar3.setColorFilter(color3, PorterDuff.Mode.MULTIPLY);
		monodollar3.setBounds(0, 0, (int) (text2.getTextSize()), (int) (text2.getTextSize()));
		text3.setCompoundDrawables(null, null, monodollar3, null);
		text1.setText(sstr1);
		text2.setText(sstr2);
		text3.setText(sstr3);
		rl1.addView(text1);
		rl1.addView(text2);
		rl2.addView(text3);
		RelativeLayout.LayoutParams p1 = (RelativeLayout.LayoutParams) text1.getLayoutParams();
		RelativeLayout.LayoutParams p2 = (RelativeLayout.LayoutParams) text2.getLayoutParams();
		RelativeLayout.LayoutParams p3 = (RelativeLayout.LayoutParams) text3.getLayoutParams();
		p1.addRule(RelativeLayout.CENTER_VERTICAL);
		p2.addRule(RelativeLayout.CENTER_VERTICAL);
		p2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		p3.addRule(RelativeLayout.CENTER_VERTICAL);
		p3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		text1.setLayoutParams(p1);
		text2.setLayoutParams(p2);
		text3.setLayoutParams(p3);
		RelativeLayout rlparent = new RelativeLayout(getApplicationContext());
		rlparent.addView(rl1);
		rlparent.addView(rl2);
		rlparent.setPadding((int) text1.getTextSize(), (int) text1.getTextSize() / 2, (int) text1.getTextSize(), (int) text1.getTextSize() / 2);
		int id = new Random().nextInt(99999999);
		rl1.setId(id);
		RelativeLayout.LayoutParams prl2 = (RelativeLayout.LayoutParams) rl2.getLayoutParams();
		prl2.addRule(RelativeLayout.BELOW, id);
		return (rlparent);
	}

	public interface VolleyCallback
	{
		public void onSuccess(JSONObject result, Boolean error);
	}

	public void logger(String txt)
	{
		logger(txt, false);
	}

	public void logger(final String txt, Boolean append)
	{
		final TextView textLog = (TextView) findViewById(R.id.textLog);
		if (append)
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					textLog.append(txt + "\n");
				}
			});
		}
		else
		{
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					textLog.setText(txt + "\n");
				}
			});
		}
	}

	private void askForPermission(String permission, Integer requestCode)
	{
		if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED)
		{
			requestPermissions(new String[]{permission}, requestCode);
		}
	}

	public Bitmap drawableToBitmap(Drawable drawable)
	{
		Bitmap bitmap = null;

		if (drawable instanceof BitmapDrawable)
		{
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if (bitmapDrawable.getBitmap() != null)
			{
				return bitmapDrawable.getBitmap();
			}
		}
		if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)
		{
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
		}
		else
		{
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public void startScanQR()
	{
		askForPermission(Manifest.permission.CAMERA, CAMERA);
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		Fragment fragment = new Scanner();
		fragmentTransaction.replace(R.id.all, new Scanner()).addToBackStack("tag").commit();
	}

	public void setQR(String read, Fragment toClose)
	{
		getSupportFragmentManager().beginTransaction().remove(toClose).commit();
		logger(read);
		if (qrUser.isEmpty())
		{
			Map<String, String> params = new HashMap<>();
			params.put("qr", read);
			getData(new VolleyCallback()
			{
				@Override
				public void onSuccess(JSONObject out, Boolean success)
				{
					if (success)
					{
						final String qrUser = JsonToString(out).get("user");
						if (!qrUser.equals(user))
						{
							//Request payment
							final AlertDialog.Builder PayInput = new AlertDialog.Builder(MainActivity.this);
							PayInput.setTitle("Preleva da " + qrUser);
							final EditText PayIn = new EditText(MainActivity.this);
							PayIn.setInputType(InputType.TYPE_CLASS_NUMBER);
							Drawable monodollar = getDrawable(R.drawable.monodollar);
							monodollar.setColorFilter(useDarkTheme ? ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_dark) : ContextCompat.getColor(MainActivity.this,android.R.color.primary_text_light), PorterDuff.Mode.MULTIPLY);
							monodollar.setBounds(0, 0, (int) PayIn.getTextSize(), (int) PayIn.getTextSize());
							PayIn.setCompoundDrawables(null, null, monodollar, null);
							PayInput.setView(PayIn);
							PayInput.setPositiveButton("OK", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
								}
							});
							PayInput.setNeutralButton("Annulla", new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
								}
							});
							final AlertDialog PayInputEdit = PayInput.create();
							PayInputEdit.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
							PayInputEdit.show();
							PayInputEdit.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View v)
								{
									String value = PayIn.getText().toString();
									if (!value.isEmpty())
									{
										Map<String, String> payparams = new HashMap<>();
										payparams.put("from", qrUser);
										payparams.put("to", user);
										payparams.put("value", value);
										payparams.put("verified", "1");
										getData(new VolleyCallback()
										{
											@Override
											public void onSuccess(JSONObject out, Boolean success)
											{
												if (success)
												{
													PayInputEdit.dismiss();
												}
											}
										}, "setPayment", payparams);
									}
								}
							});
						}
						else
						{
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
							try
							{
								intent.setPackage("com.google.android.youtube");
								MainActivity.this.startActivity(intent);
							} catch (ActivityNotFoundException e)
							{
								intent.setPackage(null);
								MainActivity.this.startActivity(intent);
							}
						}
					}
				}
			}, "getUserFromQR", params);
		}
		else
		{
			Map<String, String> params = new HashMap<>();
			params.put("user", qrUser);
			params.put("qr", read);
			getData(new VolleyCallback()
			{
				@Override
				public void onSuccess(JSONObject result, Boolean success)
				{
					if (success)
					{
						AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
						alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
							}
						});
						alertDialog.setTitle("Completato");
						alertDialog.setMessage("QR di " + qrUser + " registrato");
						alertDialog.show();
						qrUser = "";
					}
				}
			}, "setQRForUser", params);
		}
	}

	public static class Scanner extends Fragment
	{
		private CodeScanner mCodeScanner;

		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
		{
			final Activity activity = getActivity();
			final Fragment me = this;
			View root = inflater.inflate(R.layout.fragment_main, container, false);
			CodeScannerView scannerView = root.findViewById(R.id.scanner_view);
			mCodeScanner = new CodeScanner(activity, scannerView);
			mCodeScanner.setDecodeCallback(new DecodeCallback()
			{
				@Override
				public void onDecoded(@androidx.annotation.NonNull final com.google.zxing.Result result)
				{
					activity.runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							((MainActivity) getActivity()).setQR(result.getText(), me);
						}
					});
				}
			});
			scannerView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					mCodeScanner.startPreview();
				}
			});
			return root;
		}

		@Override
		public void onResume()
		{
			super.onResume();
			mCodeScanner.startPreview();
		}

		@Override
		public void onPause()
		{
			mCodeScanner.releaseResources();
			super.onPause();
		}
	}

}