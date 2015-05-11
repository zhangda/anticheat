package cn.rfidcn.anticheat.v2.ban;

import java.util.List;

public interface Banable<K> {
	
	public void doBan(List<K> ids);

}
