import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class permissionRecord extends JPanel{
	
	JCheckBox jck;
	JLabel bn;
	boolean flag;
	permissionRecord(String Flag,String colname)
	{
		flag=Flag.equals("Y");
		setLayout(new FlowLayout(FlowLayout.LEFT,10,10));
		this.setMaximumSize(new Dimension(450, 35));

		jck=new JCheckBox();
		jck.setSelected(flag);
		bn=new JLabel(colname);
		add(jck);
		add(bn);
		this.setMaximumSize(new Dimension(450, 35));
	}
	public boolean isSelectd() {
		return jck.isSelected();
	}
	public String getName()
	{
		return bn.getText();
	}
	public void setSelect(boolean b) {
		this.jck.setSelected(b);		
	}
	
}
