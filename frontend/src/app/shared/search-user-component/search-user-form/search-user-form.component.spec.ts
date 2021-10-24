import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchUserFormComponent } from './search-user-form.component';

describe('SearchUserFormComponent', () => {
  let component: SearchUserFormComponent;
  let fixture: ComponentFixture<SearchUserFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SearchUserFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchUserFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
