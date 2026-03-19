// import 'core-js/stable';
// import 'regenerator-runtime/runtime';

import { CustomLocale } from 'flatpickr/dist/types/locale';
import { Arabic } from 'flatpickr/dist/l10n/ar';
import { French } from 'flatpickr/dist/l10n/fr';
import flatpickr from 'flatpickr';

export class FlatpickrInitializer {
  private readonly locales: Record<string, CustomLocale> = {
    ar: Arabic,
    fr: French,
  };

  constructor() {
    const inputs = document.querySelectorAll<HTMLInputElement>('.flatpickr');
    if (inputs.length === 0) {
      return;
    }
    inputs.forEach(this.initFlatpickr);
  }

  private initFlatpickr = (el: HTMLInputElement) => {
    const locale = this.locales[el.dataset.locale || 'en'] ?? {};
    flatpickr(el, {
      altFormat: this.getLocalFormat(locale),
      altInput: true,
      allowInput: true,
      dateFormat: 'Y-m-d',
      locale: {
        // ...flatpickr.l10ns.ar,
        ...locale,
        firstDayOfWeek: 1,
      },
    });
  };

  private getLocalFormat = (locale: CustomLocale): string => {
    switch (locale) {
      case French:
        return 'd/m/Y';
      default:
        return 'Y/m/d';
    }
  };
}

new FlatpickrInitializer();
