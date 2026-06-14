import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let message = 'Ocurrió un error inesperado.';
      if (error.status === 0) {
        message = 'El servidor backend no está disponible. Por favor, revise que el servicio esté corriendo.';
      } else if (error.error && error.error.message) {
        message = error.error.message;
      }
      return throwError(() => new Error(message));
    })
  );
};
