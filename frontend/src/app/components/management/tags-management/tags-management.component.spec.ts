import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TagsManagementComponent } from './tags-management.component';

describe('TagsManagementComponent', () => {
  let component: TagsManagementComponent;
  let fixture: ComponentFixture<TagsManagementComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [TagsManagementComponent],
    }).compileComponents();
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
