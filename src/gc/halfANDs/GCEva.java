package gc.halfANDs;

import flexsc.Flag;
import flexsc.Mode;
import gc.GCEvaComp;
import gc.GCSignal;
import network.Network;

public class GCEva extends GCEvaComp {
	Garbler gb;

	public GCEva(Network channel) {
		super(channel, Mode.OPT);
		gb = new Garbler();
	}

	public GCSignal and(GCSignal a, GCSignal b) {
		++Flag.sw.ands;
		Flag.sw.startGC();
		GCSignal res;
		if (a.isPublic() && b.isPublic())
			res = ((a.v && b.v)? _ONE: _ZERO);
		else if (a.isPublic())
			res =  a.v ? b : _ZERO;
		else if (b.isPublic())
			res = b.v ? a : _ZERO;
		else {
			int i0 = a.getLSB();
			int i1 = b.getLSB();

			GCSignal TG = GCSignal.ZERO, WG, TE = GCSignal.ZERO, WE;
			try {
				Flag.sw.startGCIO();
				TG = GCSignal.receive(channel);
				TE = GCSignal.receive(channel);
				Flag.sw.stopGCIO();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}

			WG = gb.hash(a, gid, false).xor((i0 == 1) ? TG : GCSignal.ZERO);
			WE = gb.hash(b, gid, true).xor((i1 == 1) ? (TE.xor(a)) : GCSignal.ZERO);
			
			res = WG.xor(WE);
			
			gid++;
		}
		Flag.sw.stopGC();
		return res;
	}
}