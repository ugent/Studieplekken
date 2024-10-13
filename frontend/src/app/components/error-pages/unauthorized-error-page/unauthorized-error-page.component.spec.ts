import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UnauthorizedErrorPageComponent } from './unauthorized-error-page.component';

describe('UnauthorizedErrorPageComponent', () => {
  let component: UnauthorizedErrorPageComponent;
  let fixture: ComponentFixture<UnauthorizedErrorPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UnauthorizedErrorPageComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UnauthorizedErrorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
