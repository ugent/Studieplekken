import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TagsManagementComponent } from './tags-management.component';

describe('TagsManagementComponent', () => {
  let component: TagsManagementComponent;
  let fixture: ComponentFixture<TagsManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TagsManagementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TagsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
