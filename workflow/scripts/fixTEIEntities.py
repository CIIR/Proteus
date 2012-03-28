
#########
#
#  This script reads in a gzipped tei file, and creates entity tags around the actual words related to each 'rs' tagged entity. 
#
########

import gzip, re, sys


def adjustSentence(sentence, ent_type, ent_name, ent_coords):
    sentence = sentence.replace("\n", "")
    if len(ent_coords) == 1:
        co_index = sentence.find("coords=\"" + ent_coords[0] + "\"")
        start_index = sentence.rfind("<w ", 0, co_index)
        end_index = sentence.find("</w>", co_index) + len("</w>")
        sentence = sentence[:start_index] + "<" + ent_type.lower() + " name=\"" + ent_name + "\">" + sentence[start_index:end_index] + "</" + ent_type.lower() + ">" + sentence[end_index:]
        return sentence
    else:
        co_index = sentence.find("coords=\"" + ent_coords[0] + "\"")
        co2_index = sentence.find("coords=\"" + ent_coords[-1] + "\"")
        start_index = sentence.rfind("<w ", 0, co_index)
        end_index = sentence.find("</w>", co2_index) + len("</w>")
        sentence = sentence[:start_index] + "<" + ent_type.lower() + " name=\"" + ent_name + "\">" + sentence[start_index:end_index] + "</" + ent_type.lower() + ">" + sentence[end_index:]
        return sentence


def processSentence(sentence):
    sentence = "<s>"+sentence.replace("\n", "")
    entities = sentence.split("<rs ")[1:]
    for entity_tag in entities:
        entity_tag = entity_tag[:entity_tag.index(">")]
        if entity_tag.find("+") >= 0:
            entity_tag = entity_tag.replace("+", "\\+")

        m = re.search('type=\"(.*)\" name=\"(.*)\" coords=\"(.*)\"', entity_tag)
        ent_type = m.group(1)
        ent_name = m.group(2)
        ent_coords = m.group(3).split("|")

        if ent_type == "MISC":
            ent_type = "ORGANIZATION"

        sentence = adjustSentence(sentence, ent_type, ent_name, ent_coords)
    return sentence


print sys.argv
f = gzip.open(sys.argv[1], 'rb')
o = gzip.open(sys.argv[2], 'wb')

buffer = ""
for line in f:
    splits = line.split("<s>")
    buffer += splits[0]
    for full_sentence in splits[1:]:
        o.write(processSentence(buffer))
        buffer = full_sentence
        
o.write(processSentence(buffer))
f.close()
o.close()

