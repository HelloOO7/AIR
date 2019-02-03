import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import redlaboratory.jvjoyinterface.*;

public class Interface {
	public static VJoy v;
	public static int id;
	public static Socket soc;
	public static Socket soc2;
	public static ServerSocket ss;
	public static ServerSocket ss2;
	public static void main(String[] args) throws VJoyException{
		v = new VJoy();
		//id = (args[0] != null && Integer.parseInt(args[0]) > 0 && Integer.parseInt(args[0]) <= 16) ? Integer.parseInt(args[0]) : 1;
		id = 2;
		if (v.vJoyEnabled()) {
			System.out.println("VJoy is enabled, communicating with driver...");
			System.out.println("VJoy Interface running with version " + v.getvJoyVersion());
		}
		else {
			throw new VJoyException("Could not connect to driver");
		}	
		VjdStat s = v.getVJDStatus(id);
		switch (s) {
			case VJD_STAT_BUSY:
				System.out.println("vJoy device is busy, assigning closest possible candidate");
				panicAssign();
				break;
			case VJD_STAT_FREE:
				System.out.println("The device is available for acquisition");
				break;
			case VJD_STAT_OWN:
				System.out.println("AndFeeder already has a VJoy device assigned");
				break;
			case VJD_STAT_MISS:
				throw new VJoyException("The VJoy device is either missing or the driver could not be contacted");
			case VJD_STAT_UNKN:
				System.out.println("WARN: Device status unknown");
				//throw new VJoyException("Unknown VJoy error");
		}
		v.acquireVJD(id);
		String dm = (v.driverMatch()) ? "The vJoy driver's version matches that of the interface (2.1.8)" :
			"The vJoy driver's version differs from JvJoyInterface's. Either install vJoy 2.1.8 or continue on your own risk.";
		System.out.println(dm);
		//setup axes and buttonz
		if (v.getVJDButtonNumber(id) < 14) {
			throw new VJoyException("Not enough buttons! Please configure VJoy Device #" + id + " with at least 12 buttons");
		};
		clearJS();
		try {
			ss = new ServerSocket(4269);
			ss2 = new ServerSocket(4270);
			soc = ss.accept();
			soc2 = ss2.accept();
			InputStream is = soc.getInputStream();
			InputStream is2 = soc2.getInputStream();
			Thread t = new Thread() {
				public float fromByte(int bvalue) {
				     return (float)bvalue/100f;
				}
				public void run() {
					long maxAxisX = v.getVJDAxisMax(id, VJoy.HID_USAGE_X);
					long maxAxisY = v.getVJDAxisMax(id, VJoy.HID_USAGE_Y);
					long maxAxisRX = v.getVJDAxisMax(id, VJoy.HID_USAGE_RX);
					long maxAxisRY = v.getVJDAxisMax(id, VJoy.HID_USAGE_RY);
					long maxTriggerL = v.getVJDAxisMax(id, VJoy.HID_USAGE_Z);
					long maxTriggerR = v.getVJDAxisMax(id, VJoy.HID_USAGE_RZ);
					byte[] b = new byte[8];
					while (true) {
						try {
							if (is2.available()/8 != 0) {
								is2.read(b, 0, 8);
								boolean b1 = false;
								switch (b[0]) {
									case -1:
										b1 = true;
									case 1:
										v.setBtn(b1, id, 9);
										v.setBtn(!b1, id, 10);
										break;
									case 0:
										v.setBtn(false, id, 9);
										v.setBtn(false, id, 10);
								}
								switch (b[1]) {
									case -1:
										b1 = true;
									case 1:
										v.setBtn(b1, id, 7);
										v.setBtn(!b1, id, 8);
										break;
									case 0:
										v.setBtn(false, id, 7);
										v.setBtn(false, id, 8);
								}
								/*byte[] b2 = new byte[4];
								byte[] b3 = new byte[4];
								System.arraycopy(b, 2, b2, 0, 4);
								System.arraycopy(b, 6, b3, 0, 4);*/
								float xvalue = fromByte(b[2]);
								float yvalue = fromByte(b[3]);
								float rxvalue = fromByte(b[4]);
								float ryvalue = fromByte(b[5]);
								float ltrigger = fromByte(b[6]);
								float rtrigger = fromByte(b[7]);
								//System.out.println((long)(maxAxisX/2 + (maxAxisX/2)*xvalue));
								v.setAxis((long)(maxAxisX/2 + (maxAxisX/2)*xvalue), id, VJoy.HID_USAGE_X);
								v.setAxis((long)(maxAxisY/2 + (maxAxisY/2)*yvalue), id, VJoy.HID_USAGE_Y);
								System.out.println(rxvalue);
								v.setAxis((long)(maxAxisRX/2 + (maxAxisRX/2)*rxvalue), id, VJoy.HID_USAGE_RX);
								v.setAxis((long)(maxAxisRY/2 + (maxAxisRY/2)*ryvalue), id, VJoy.HID_USAGE_RY);
								v.setAxis((long)(maxTriggerL/2 + (maxTriggerL/2)*ltrigger), id, VJoy.HID_USAGE_Z);
								v.setAxis((long)(maxTriggerR/2 + (maxTriggerR/2)*rtrigger), id, VJoy.HID_USAGE_RZ);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			};
			t.start();
			while (true) {
				int isDownByte = is.read();
				int keycodeByte = is.read();
				if (isDownByte != -1) {
					System.out.println(keycodeByte);
					v.setBtn((isDownByte == 1) ? true : false, id, keycodeByte + 1);
				}
			}				
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				ss.close();
				ss2.close();
				soc.close();
				soc2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	public static void clearJS() {
		v.setAxis(v.getVJDAxisMax(id, VJoy.HID_USAGE_X)/2, id, VJoy.HID_USAGE_X);
		v.setAxis(v.getVJDAxisMax(id, VJoy.HID_USAGE_Y)/2, id, VJoy.HID_USAGE_Y);
		v.setAxis(v.getVJDAxisMax(id, VJoy.HID_USAGE_Z)/2, id, VJoy.HID_USAGE_Z);
		v.setAxis(v.getVJDAxisMax(id, VJoy.HID_USAGE_RX)/2, id, VJoy.HID_USAGE_RX);
		v.setAxis(v.getVJDAxisMax(id, VJoy.HID_USAGE_RY)/2, id, VJoy.HID_USAGE_RY);
		v.setAxis(v.getVJDAxisMax(id, VJoy.HID_USAGE_RZ)/2, id, VJoy.HID_USAGE_RZ);
		v.setAxis(v.getVJDAxisMax(id, VJoy.HID_USAGE_SL0)/2, id, VJoy.HID_USAGE_SL0);
		v.setAxis(v.getVJDAxisMax(id, VJoy.HID_USAGE_SL1)/2, id, VJoy.HID_USAGE_SL1);
		for (int i = 1; i <= 16; i++) {
			v.setBtn(false, id, i);
		}
	}
	public static void panicAssign() throws VJoyException {
		for (int i = 1; i < 17; i++) {
			if (v.getVJDStatus(i) == VjdStat.VJD_STAT_FREE || v.getVJDStatus(i) == VjdStat.VJD_STAT_OWN){
				id = i;
				break;
			}
		}
		throw new VJoyException("No free VJoy device found");
	}
}
