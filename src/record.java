import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class record extends JPanel{
	
	private static final long serialVersionUID = 1L;


	public record(String Flag,String colName,String chinese,int no){
		setLayout(new FlowLayout(FlowLayout.LEFT,10,10));
		this.setMaximumSize(new Dimension(450, 35));

		jck=new JCheckBox();
		bn=new JLabel();
		zwm=new JTextField(25);
		xh=new JTextField(3);

		if (Flag.equals("Y")) {
			jck.setSelected(true);
		}
		else jck.setSelected(false);

		bn.setPreferredSize(new Dimension(100, 35));		
		bn.setText(colName);
		zwm.setText(chinese);
		xh.setText(no+"");
		add(jck);
		add(bn);
		add(new JLabel("翻译"));
		add(zwm);
		add(xh);
	}
	public record(String colName)
	{
		this("N",colName,"",0);
	}
	public String getChinese(){
		return zwm.getText();
	}
	public String getFlag(){
		return jck.isSelected()?"Y":"N";
	}
	public int getxh(){
		return Integer.parseInt(xh.getText());
	}
	public String getColName(){
		return bn.getText();
	}
	public void reset( String flag, String chinese, String xh) {
		boolean f=flag=="Y"?true:false;
		jck.setSelected(f);
		zwm.setText(chinese);
		this.xh.setText(xh);
	}
	public void clear()
	{
		jck.setSelected(false);
		zwm.setText("");
		xh.setText("0");
	}
	JTextField zwm;
	JTextField xh;
	JCheckBox jck;
	JLabel bn;



}


