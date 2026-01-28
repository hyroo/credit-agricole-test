.PHONY: all
all:
	./gradlew build

.PHONY: clean
clean:
	./gradlew clean

.PHONY: reset-deps
reset-deps:
	./gradlew clean --refresh-dependencies

.PHONY: compose-res
compose-res:
	./gradlew composeApp:generateComposeRes

.PHONY: assemble
assemble:
	./gradlew composeApp:assemble

.PHONY: android-debug
android-debug:
	./gradlew :composeApp:assembleDebug
