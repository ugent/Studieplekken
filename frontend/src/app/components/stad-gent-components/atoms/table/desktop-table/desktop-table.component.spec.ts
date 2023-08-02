import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DesktopTableComponent } from './desktop-table.component';

describe('DesktopTableComponent', () => {
  let component: DesktopTableComponent;
  let fixture: ComponentFixture<DesktopTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DesktopTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DesktopTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
