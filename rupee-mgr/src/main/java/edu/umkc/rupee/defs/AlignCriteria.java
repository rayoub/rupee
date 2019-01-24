package edu.umkc.rupee.defs;

import org.biojava.nbio.structure.align.ce.CECPParameters;
import org.biojava.nbio.structure.align.ce.CeCPMain;
import org.biojava.nbio.structure.align.ce.CeMain;
import org.biojava.nbio.structure.align.ce.CeParameters;
import org.biojava.nbio.structure.align.ce.ConfigStrucAligParams;
import org.biojava.nbio.structure.align.fatcat.FatCatFlexible;
import org.biojava.nbio.structure.align.fatcat.FatCatRigid;
import org.biojava.nbio.structure.align.fatcat.calc.FatCatParameters;

public enum AlignCriteria {

    CE(CeMain.algorithmName, new CeParameters()),
    CECP(CeCPMain.algorithmName, new CECPParameters()),
    FATCAT_FLEXIBLE(FatCatFlexible.algorithmName, new FatCatParameters()),
    FATCAT_RIGID(FatCatRigid.algorithmName, new FatCatParameters()),
    TM_Q_ALIGN(null, null),
    TM_AVG_ALIGN(null, null);

    private String algorithmName;
    private ConfigStrucAligParams params; 

    AlignCriteria(String algorithmName, ConfigStrucAligParams params) {
        this.algorithmName = algorithmName;
        this.params = params;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public ConfigStrucAligParams getParams() {
        return params;
    }
}
