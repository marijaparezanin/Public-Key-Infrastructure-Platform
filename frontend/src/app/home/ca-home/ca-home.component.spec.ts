import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaHomeComponent } from './ca-home.component';

describe('CaHomeComponent', () => {
  let component: CaHomeComponent;
  let fixture: ComponentFixture<CaHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
