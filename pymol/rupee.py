import csv

from pymol import stored
from pymol import cmd

def label_residues(entity, color):

    with open(entity + '.labels') as label_file:
        reader = csv.reader(label_file)
        labels = {int(rows[0]):rows[1] for rows in reader}

    selection_model = '/' + entity + '////ca' 
    selection_atom = '/' + entity + '///{0}/{1}'

    cmd.set('label_size', 20, selection_model)
    cmd.set('label_color', color, selection_model)

    model = cmd.get_model(selection_model, 1)
    for i in range(0, len(model.atom)):
        residue_number = int(model.atom[i].resi)
        if residue_number in labels:
            cmd.label(selection_atom.format(residue_number, model.atom[i].name), labels[residue_number])

# register
cmd.extend(label_residues)
