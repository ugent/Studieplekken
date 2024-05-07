import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FaqSidebarItemComponent } from './faq-sidebar-item.component';

describe('FaqSidebarItemComponent', () => {
  let component: FaqSidebarItemComponent;
  let fixture: ComponentFixture<FaqSidebarItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FaqSidebarItemComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FaqSidebarItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
