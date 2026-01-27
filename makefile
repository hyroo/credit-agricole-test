# Contenu de la liste à exclure du coverage
EXCLUDED_FILES_CONTENT := \
	'lib/app/constants/*' \
	'lib/app/router/*' \
	'lib/helpers/*' \
	'lib/bases/extensions/*' \
	'lib/app/app_controller.dart' \
	'lib/app/app_widget.dart' \
	'lib/app/static/*' \
	'lib/generated/*' \
	'lib/managers/*' \

.PHONY: all
all:
	./gradlew build

.PHONY: clean
clean:
	./gradlew clean
	./gradlew clean --refresh-dependencies

.PHONY: release
release: clean
	flutter build apk --release -t lib/main.dart