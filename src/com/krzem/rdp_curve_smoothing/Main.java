package com.krzem.rdp_curve_smoothing;



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.Exception;
import java.lang.Math;
import javax.swing.JFrame;



public class Main{
	public static void main(String[] args){
		new Main(args);
	}



	public static final int DISPLAY_ID=0;
	public static final GraphicsDevice SCREEN=GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[DISPLAY_ID];
	public static final Rectangle WINDOW_SIZE=SCREEN.getDefaultConfiguration().getBounds();
	public static final int MAX_FPS=60;
	public double FPS=1;
	public double EPSILON=0;
	public JFrame frame;
	public Canvas canvas;
	public double[][] points;
	public double[][] rdp_points;
	private Runnable _ru;
	private boolean _break=false;



	public Main(String[] args){
		this.init();
		this.frame_init();
		this.run();
	}



	public void init(){
		this.points=new double[WINDOW_SIZE.width][2];
		for (int i=0;i<WINDOW_SIZE.width;i+=10){
			double v=this._map(i,0,WINDOW_SIZE.width,0,5);
			this.points[i]=new double[]{i,this._map(Math.exp(-v)*Math.cos(Math.PI*2*v),-1,1,WINDOW_SIZE.height,0)};
		}
	}



	public void frame_init(){
		Main cls=this;
		this.frame=new JFrame("RDP Curve Smoothing");
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setUndecorated(true);
		this.frame.setResizable(false);
		this.frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				cls.quit();
			}
		});
		SCREEN.setFullScreenWindow(this.frame);
		this.canvas=new Canvas(this);
		this.canvas.setSize(WINDOW_SIZE.width,WINDOW_SIZE.height);
		this.canvas.setPreferredSize(new Dimension(WINDOW_SIZE.width,WINDOW_SIZE.height));
		this.frame.setContentPane(this.canvas);
		this.canvas.requestFocus();
	}



	public void run(){
		Main cls=this;
		this._ru=new Runnable(){
			@Override
			public void run(){
				while (cls._break==false){
					long s=System.currentTimeMillis();
					try{
						cls.canvas.repaint();
					}
					catch (Exception e){
						e.printStackTrace();
					}
					long d=System.currentTimeMillis()-s;
					if (d==0){
						d=1L;
					}
					if ((double)Math.floor(1/(double)d*1e8)/1e5>cls.MAX_FPS){
						try{
							Thread.sleep((long)(1/(double)cls.MAX_FPS*1e3)-d);
						}
						catch (InterruptedException e){}
					}
					cls.FPS=(double)Math.floor(1/(double)(System.currentTimeMillis()-s)*1e8)/1e5;
				}
			}
		};
		new Thread(this._ru).start();
	}



	public void draw(Graphics2D g){
		EPSILON+=0.1;
		if (EPSILON>=100){
			EPSILON=0;
		}
		g.setColor(new Color(0,0,0));
		g.fillRect(0,0,WINDOW_SIZE.width,WINDOW_SIZE.height);
		g.setColor(new Color(255,255,255));
		g.setStroke(new BasicStroke(10));
		this._draw_graph(g,this.points);
		g.setColor(new Color(255,0,128));
		this._draw_graph(g,this.RDP(this.points));
	}



	private void _draw_graph(Graphics2D g,double[][] pl){
		int[] xp=new int[pl.length];
		int[] yp=new int[pl.length];
		for (int i=0;i<pl.length;i++){
			xp[i]=(int)pl[i][0];
			yp[i]=(int)pl[i][1];
		}
		g.drawPolyline(xp,yp,xp.length);
	}



	private double[][] RDP(double[][] pl){
		double md=0;
		int mdi=0;
		for (int i=1;i<pl.length;i++){
			double d=this._point_line_dist(pl[i],pl[0],pl[pl.length-1]);
			if (d>md){
				md=d;
				mdi=i+0;
			}
		}
		if (md>EPSILON){
			double[][] pla=new double[mdi][2];
			double[][] plb=new double[pl.length-mdi][2];
			for (int i=0;i<pl.length;i++){
				if (i<mdi){
					pla[i]=pl[i];
				}
				else{
					plb[i-mdi]=pl[i];
				}
			}
			double[][] a=this.RDP(pla);
			double[][] b=this.RDP(plb);
			double[][] o=new double[a.length-1+b.length][2];
			for (int i=0;i<b.length-1;i++){
				if (i<a.length-1){
					o[i]=a[i];
				}
				else{
					o[i]=b[i-(a.length-1)];
				}
			}
			return o;
		}
		else{
			return new double[][]{pl[0],pl[pl.length-1]};
		}
	}



	private double _point_line_dist(double[] p,double[] a,double[] b){
		double lx=(b[0]-a[0]);
		double ly=(b[1]-a[1]);
		double lmg=lx*lx+ly*ly;
		lx/=lmg;
		ly/=lmg;
		double pdx=(p[0]-a[0]);
		double pdy=(p[1]-a[1]);
		double dt=pdx*lx+pdy*ly;
		lx*=dt;
		ly*=dt;
		lx+=a[0];
		ly+=a[1];
		double dx=lx-p[0];
		double dy=ly-p[1];
		return Math.sqrt(dx*dx+dy*dy);
	}



	private void quit(){
		if (this._break==true){
			return;
		}
		this._break=true;
		this.frame.dispose();
		this.frame.dispatchEvent(new WindowEvent(this.frame,WindowEvent.WINDOW_CLOSING));
	}



	private double _map(double v,double aa,double ab,double ba,double bb){
		return (v-aa)/(ab-aa)*(bb-ba)+ba;
	}
}
