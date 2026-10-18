package dev.playwright.context;
import dev.playwright.context.model.RelevanceOverride;
import dev.playwright.context.model.RelevanceSettings;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class RelevanceSettingsTest {
    final RelevanceSettings defaults=new RelevanceSettings(10,4,3,3,1,2,200,12,3000);
    @Test void missingFieldsInMongoLeaveDefaultsUntouchedAndZeroIsExplicit() {
        var override=new RelevanceOverride();
        override.primaryTagWeight=0.0;
        override.maxEvents=3;
        var effective=RelevanceSettingsService.merge(defaults,override);
        assertThat(effective.primaryTagWeight()).isZero();
        assertThat(effective.secondaryTagWeight()).isEqualTo(4);
        assertThat(effective.maxEvents()).isEqualTo(3);
        assertThat(effective.defaultTokenBudget()).isEqualTo(3000);
    }
    @Test void invalidMergedWeightIsRejected() {
        var override=new RelevanceOverride();
        override.primaryTagWeight=Double.NaN;
        assertThatThrownBy(()->RelevanceSettingsService.merge(defaults,override))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
