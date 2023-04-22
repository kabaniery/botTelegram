import sys
import os

ishod = sorted(list({'Биология', 'Астрономия', 'Наука', 'Общий курс физики. Сборник задач', 'Астрономия и Космос', 'Учебная литература', 'Общая философия', 'Физика', 'Математика', 'Учебная', 'Религия', 'Физика ', 'методическая литература и словари', 'Physics', 'Военная техника и вооружение', 'Интервью', 'Биографии и мемуары'}))
file = open(sys.argv[1])
result = 0
for i in file:
	if i[len(i)-1] == "\n":
		i = i[:len(i)-1]
	if i in ishod:
		result += 2**(ishod.index(i))
file.close()
os.remove(sys.argv[1])
print(result)